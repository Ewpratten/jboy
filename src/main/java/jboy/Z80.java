package jboy;

public class Z80 {
    public static class Registers {
        static int a, b, c, d, e, h, l, f = 0;
        static int sp, pc, i, r = 0;
        static int m = 0;
        static int ime = 0;
    }

    static Registers r = null;

    public static Registers getRegisters() {
        if (r == null) {
            r = new Registers();
        }

        return r;
    }
}