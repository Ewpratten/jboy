package jboy;

import java.awt.Color;

import PicoEngine.Window;

public class GPU {

    final int pixel_size = 4;
    Window win = new Window(160 * pixel_size, 144 * pixel_size, 1, 1, "jBoy");

    public int[] vram = new int[8192];
    public int[] oam = new int[160];
    public int[] reg = new int[25600];
    public int[][][] tilemap = new int[512][8][8];

    int curline = 0;
    int curscan = 0;
    int linemode = 2;
    int modeclocks = 0;
    int yscrl = 0;
    int xscrl = 0;
    int raster = 0;
    int ints = 0;

    boolean lcdon = true;
    boolean bgon = true;
    boolean objon = true;
    boolean winon = true;

    int objsize = 0;

    int bgtilebase = 0x0000;
    int bgmapbase = 0x1800;
    int wintilebase = 0x1800;

    int[] scanrow = new int[160];

    int[][] screen = new int[160 * 144][4];

    private class Palette {
        int[] bg = new int[4];
        int[] obj0 = new int[4];
        int[] obj1 = new int[4];
    }

    private class Object {
        int y = -16;
        int x = -8;

        int tile = 0;
        int palette = 0;

        int xflip = 0;
        int yflip = 0;

        int prio = 0;

        int num;

        public Object(int num) {
            this.num = num;
        }
    }

    Palette palette = new Palette();
    Object[] objdata = new Object[40];

    Object[] objdatasorted = new Object[40];

    Z80 z80;
    MMU mmu;

    public GPU(Z80 z80, MMU mmu) {
        this.z80 = z80;
        this.mmu = mmu;
    }

    public void setZ80(Z80 z80) {
        this.z80 = z80;
    }

    public void setMMU(MMU mmu) {
        this.mmu = mmu;
    }

    public void reset() {
        for (int i = 0; i < 8192; i++) {
            vram[i] = 0;
        }
        for (int i = 0; i < 160; i++) {
            oam[i] = 0;
        }
        for (int i = 0; i < 4; i++) {
            palette.bg[i] = 255;
            palette.obj0[i] = 255;
            palette.obj1[i] = 255;
        }
        for (int i = 0; i < 512; i++) {
            for (int j = 0; j < 8; j++) {
                for (int k = 0; k < 8; k++) {
                    tilemap[i][j][k] = 0;
                }
            }
        }

        System.out.println("Starting GC");
        win.autoConfig(Color.gray);

        curline = 0;
        curscan = 0;
        linemode = 2;
        modeclocks = 0;
        yscrl = 0;
        xscrl = 0;
        raster = 0;
        ints = 0;

        lcdon = false;
        bgon = false;
        objon = false;
        winon = false;

        objsize = 0;
        for (int i = 0; i < 160; i++) {
            scanrow[i] = 0;
        }

        for (int i = 0; i < 40; i++) {
            objdata[i] = new Object(i);
        }

        // Set to values expected by BIOS, to start
        bgtilebase = 0x0000;
        bgmapbase = 0x1800;
        wintilebase = 0x1800;

        System.out.println("GPU reset");
    }

    public void checkLine() {
        modeclocks += z80.r.m;
        switch (linemode) {
        // In hblank
        case 0:
            if (modeclocks >= 51) {
                // End of hblank for last scanline; render screen
                if (curline == 143) {
                    linemode = 1;
                    draw(screen);
                    mmu._if |= 1;
                } else {
                    linemode = 2;
                }
                curline++;
                curscan += 640;
                modeclocks = 0;
            }
            break;

        // In vblank
        case 1:
            if (modeclocks >= 114) {
                modeclocks = 0;
                curline++;
                if (curline > 153) {
                    curline = 0;
                    curscan = 0;
                    linemode = 2;
                }
            }
            break;

        // In OAM-read mode
        case 2:
            if (modeclocks >= 20) {
                modeclocks = 0;
                linemode = 3;
            }
            break;

        // In VRAM-read mode
        case 3:
            // Render scanline at end of allotted time
            if (modeclocks >= 43) {
                modeclocks = 0;
                linemode = 0;
                if (lcdon) {
                    if (bgon) {
                        int linebase = curscan;
                        int mapbase = bgmapbase + ((((curline + yscrl) & 255) >> 3) << 5);
                        int y = (curline + yscrl) & 7;
                        int x = xscrl & 7;
                        int t = (xscrl >> 3) & 31;
                        int pixel;
                        int w = 160;

                        if (bgtilebase != 0) {
                            int tile = vram[mapbase + t];
                            if (tile < 128)
                                tile = 256 + tile;
                            int[] tilerow = tilemap[tile][y];
                            do {
                                scanrow[160 - x] = tilerow[x];
                                screen[linebase][0] = palette.bg[tilerow[x]];
                                x++;
                                if (x == 8) {
                                    t = (t + 1) & 31;
                                    x = 0;
                                    tile = vram[mapbase + t];
                                    if (tile < 128)
                                        tile = 256 + tile;
                                    tilerow = tilemap[tile][y];
                                }
                                linebase += 4;
                            } while (--w != 0);
                        } else {
                            int[] tilerow = tilemap[vram[mapbase + t]][y];
                            do {
                                scanrow[160 - x - 1] = tilerow[x];
                                screen[linebase][0] = palette.bg[tilerow[x]];
                                x++;
                                if (x == 8) {
                                    t = (t + 1) & 31;
                                    x = 0;
                                    tilerow = tilemap[vram[mapbase + t]][y];
                                }
                                linebase += 4;
                            } while (--w != 0);
                        }
                    }
                    if (objon) {
                        int cnt = 0;
                        if (objsize != 0) {
                            for (int i = 0; i < 40; i++) {
                            }
                        } else {
                            int[] tilerow;
                            Object obj;
                            int[] pal;
                            int pixel;
                            int x;
                            int linebase = curscan;
                            for (int i = 0; i < 40; i++) {
                                obj = objdatasorted[i];
                                if (obj.y <= curline && (obj.y + 8) > curline) {
                                    if (obj.yflip != 0)
                                        tilerow = tilemap[obj.tile][7 - (curline - obj.y)];
                                    else
                                        tilerow = tilemap[obj.tile][curline - obj.y];

                                    if (obj.palette != 0)
                                        pal = palette.obj1;
                                    else
                                        pal = palette.obj0;

                                    linebase = (curline * 160 + obj.x) * 4;
                                    if (obj.xflip != 0) {
                                        for (x = 0; x < 8; x++) {
                                            if (obj.x + x >= 0 && obj.x + x < 160) {
                                                if (tilerow[7 - x] != 0 && (obj.prio != 0 || (scanrow[x] == 0))) {
                                                    screen[linebase][3] = pal[tilerow[7 - x]];
                                                }
                                            }
                                            linebase += 4;
                                        }
                                    } else {
                                        for (x = 0; x < 8; x++) {
                                            if (obj.x + x >= 0 && obj.x + x < 160) {
                                                if (tilerow[x] != 0 && (obj.prio != 0 || (scanrow[x] == 0))) {
                                                    screen[linebase][3] = pal[tilerow[x]];
                                                }
                                            }
                                            linebase += 4;
                                        }
                                    }
                                    cnt++;
                                    if (cnt > 10)
                                        break;
                                }
                            }
                        }
                    }
                }
            }
            break;
        }
    }

    public void updateTile(int addr, int val) {
        int saddr = addr;
        if ((addr & 1) != 0) {
            saddr--;
            addr--;
        }
        int tile = (addr >> 4) & 511;
        int y = (addr >> 1) & 7;
        int sx;
        for (int x = 0; x < 8; x++) {
            sx = 1 << (7 - x);
            tilemap[tile][y][x] = (((vram[saddr] & sx) != 0) ? 1 : 0) | (((vram[saddr + 1] & sx) != 0) ? 2 : 0);
        }
    }

    public void updateOAM(int addr, int val) {
        addr -= 0xFE00;
        int obj = addr >> 2;
        if (obj < 40) {
            switch (addr & 3) {
            case 0:
                objdata[obj].y = val - 16;
                break;
            case 1:
                objdata[obj].x = val - 8;
                break;
            case 2:
                if (objsize != 0)
                    objdata[obj].tile = (val & 0xFE);
                else
                    objdata[obj].tile = val;
                break;
            case 3:
                objdata[obj].palette = ((val & 0x10) != 0) ? 1 : 0;
                objdata[obj].xflip = ((val & 0x20) != 0) ? 1 : 0;
                objdata[obj].yflip = ((val & 0x40) != 0) ? 1 : 0;
                objdata[obj].prio = ((val & 0x80) != 0) ? 1 : 0;
                break;
            }
        }
        objdatasorted = objdata;
        // objdatasorted
        // sort(function(a,b){
        // if(a.x>b.x) return -1;
        // if(a.num>b.num) return -1;
        // });
    }

    public int rb(int addr) {
        int gaddr = addr - 0xFF40;
        switch (gaddr) {
        case 0:
            return (lcdon ? 0x80 : 0) | ((bgtilebase == 0x0000) ? 0x10 : 0) | ((bgmapbase == 0x1C00) ? 0x08 : 0)
                    | (objsize != 0 ? 0x04 : 0) | (objon ? 0x02 : 0) | (bgon ? 0x01 : 0);

        case 1:
            return (curline == raster ? 4 : 0) | linemode;

        case 2:
            return yscrl;

        case 3:
            return xscrl;

        case 4:
            return curline;

        case 5:
            return raster;

        default:
            return reg[gaddr];
        }
    }

    public void wb(int addr, int val) {
        int gaddr = addr - 0xFF40;
        reg[gaddr] = val;
        switch (gaddr) {
        case 0:
            lcdon = ((val & 0x80) != 0) ? true : false;
            bgtilebase = ((val & 0x10) != 0) ? 0x0000 : 0x0800;
            bgmapbase = ((val & 0x08) != 0) ? 0x1C00 : 0x1800;
            objsize = ((val & 0x04) != 0) ? 1 : 0;
            objon = ((val & 0x02) != 0) ? true : false;
            bgon = ((val & 0x01) != 0) ? true : false;
            break;

        case 2:
            yscrl = val;
            break;

        case 3:
            xscrl = val;
            break;

        case 5:
            raster = val;

            // OAM DMA
        case 6:
            int v;
            for (int i = 0; i < 160; i++) {
                v = mmu.rb((val << 8) + i);
                oam[i] = v;
                updateOAM(0xFE00 + i, v);
            }
            break;

        // BG palette mapping
        case 7:
            for (int i = 0; i < 4; i++) {
                switch ((val >> (i * 2)) & 3) {
                case 0:
                    palette.bg[i] = 255;
                    break;
                case 1:
                    palette.bg[i] = 192;
                    break;
                case 2:
                    palette.bg[i] = 96;
                    break;
                case 3:
                    palette.bg[i] = 0;
                    break;
                }
            }
            break;

        // OBJ0 palette mapping
        case 8:
            for (int i = 0; i < 4; i++) {
                switch ((val >> (i * 2)) & 3) {
                case 0:
                    palette.obj0[i] = 255;
                    break;
                case 1:
                    palette.obj0[i] = 192;
                    break;
                case 2:
                    palette.obj0[i] = 96;
                    break;
                case 3:
                    palette.obj0[i] = 0;
                    break;
                }
            }
            break;

        // OBJ1 palette mapping
        case 9:
            for (int i = 0; i < 4; i++) {
                switch ((val >> (i * 2)) & 3) {
                case 0:
                    palette.obj1[i] = 255;
                    break;
                case 1:
                    palette.obj1[i] = 192;
                    break;
                case 2:
                    palette.obj1[i] = 96;
                    break;
                case 3:
                    palette.obj1[i] = 0;
                    break;
                }
            }
            break;
        }
    }

    public void draw(int[][] screen) {
        synchronized (win) {
            win.clear();
            int i = 0;
            for (int[] pixel : screen) {
                win.setColor(new Color(0,0, pixel[3]));
                win.rect((i % 144) * pixel_size, (int) (i / 160) * pixel_size, pixel_size, pixel_size, true);
                i += 1;
            }

            // for (int[] x : screen) {
            //     System.out.print(x[3]);
            // }
        }
    }

    public void sleep(int ms){
        win.sleep(ms);
    }
}