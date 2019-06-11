package jboy;

public class GPU {
    public int[] vram = new int[8192];
    public int[] oam = new int[160];
    public int[][][] tilemap = new int[512][8][8];

    private class Palette {
        int[] bg = new int[4];
        int[] obj0 = new int[4];
        int[] obj1 = new int[4];
    }

    Palette palette = new Palette();

    public void reset() {
        for(int i=0; i<8192; i++) {
            vram[i] = 0;
        }
          for(int i=0; i<160; i++) {
            oam[i] = 0;
          }
          for(int i=0; i<4; i++) {
            palette.bg[i] = 255;
            palette.obj0[i] = 255;
            palette.obj1[i] = 255;
          }
          for(int i=0;i<512;i++)
          {
            tilemap[i] = [];
            for(int j=0;j<8;j++)
            {
              tilemap[i][j] = [];
              for(int k=0;k<8;k++)
              {
                tilemap[i][j][k] = 0;
              }
            }
          }
      
          System.out.println("Starting GC");
          var c = document.getElementById('screen');
          if(c && c.getContext)
          {
            canvas = c.getContext('2d');
            if(!canvas)
            {
              throw new Error('GPU: Canvas context could not be created.');
            }
            else
            {
              if(canvas.createImageData)
                scrn = canvas.createImageData(160,144);
              else if(canvas.getImageData)
                scrn = canvas.getImageData(0,0,160,144);
              else
                scrn = {'width':160, 'height':144, 'data':new Array(160*144*4)};
      
              for(i=0; i<scrn.data.length; i++)
                scrn.data[i]=255;
      
              canvas.putImageData(scrn, 0,0);
            }
          }
      
          curline=0;
          curscan=0;
          linemode=2;
          modeclocks=0;
          yscrl=0;
          xscrl=0;
          raster=0;
          ints = 0;
      
          lcdon = 0;
          bgon = 0;
          objon = 0;
          winon = 0;
      
          objsize = 0;
          for(i=0; i<160; i++) scanrow[i] = 0;
      
          for(i=0; i<40; i++)
          {
            objdata[i] = {'y':-16, 'x':-8, 'tile':0, 'palette':0, 'yflip':0, 'xflip':0, 'prio':0, 'num':i};
          }
      
          // Set to values expected by BIOS, to start
          bgtilebase = 0x0000;
          bgmapbase = 0x1800;
      wintilebase = 0x1800;
    }

    public int rb(int addr) {
        return 0;
    }

    public void updateTile(int addr, int val) {

    }

    public void updateOAM(int addr, int val) {

    }

    public void wb(int addr, int val) {

    }
}