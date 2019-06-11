package jboy;

public class Z80 {
    public static class Registers {
        static int a, b, c, d, e, h, l, f = 0;
        static int sp, pc, i, r = 0;
        static int m = 0;
        static int ime = 0;
    }

    public static class RSV {
        static int a, b, c, d, e, h, l, f = 0;
    }

    public static class Clock {
        int m = 0;
    }

    boolean halt, stop = false;

    static Registers r = null;
    RSV rsv = new RSV();
    Clock clock = new Clock();

    MMU mmu;

    public Z80(MMU mmu) {
        this.mmu = mmu;
    }

    public void setMMU(MMU mmu) {
        this.mmu = mmu;
    }

    public static Registers getRegisters() {
        if (r == null) {
            r = new Registers();
        }

        return r;
    }

    public void reset() {
        r = new Registers();
        halt = stop = false;
        clock.m = 0;
        r.ime = 1;
        System.out.println("Reset z80 emulator");
    }

    public void exec(int instruction) {
        // r.r = (r.r + 1) & 127;

        /* Im so sorry... */
        int ins = (mmu.rb(r.pc++));
        switch (ins) {

        case 0:
            r.m += 1;

        default:
            System.out.println("Unknown instruction: " + ins);
        }
    }

    public class OPS {
        public void RST40() {

        }

        public void RST48() {

        }

        public void RST50() {

        }

        public void RST58() {

        }

        public void RST60() {

        }
    }

    OPS ops = new OPS();
}