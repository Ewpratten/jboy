package jboy;

public class Timer {
    private int div, tma, tima, tac = 0;

    private static class Clock {
        static int main, sub, div = 0;
    }

    public void reset() {
        div = tma = tima = tac = 0;
        Clock.main = Clock.sub = Clock.div = 0;
        System.out.println("Timer has been reset");
    }

    public void step(MMU mmu) {
        tima += 1;

        Clock.main = 0;

        if (tima > 255) {
            tima = tma;
            mmu._if |= 4;
        }
    }

    public void inc(Z80 z80, MMU mmu) {
        int prev = Clock.main;

        Clock.sub += z80.getRegisters().m;
        if (Clock.sub > 3) {
            Clock.main += 1;
            Clock.sub -= 4;
            Clock.div += 1;

            if (Clock.div == 16) {
                Clock.div = 0;
                div += 1;
                div &= 255;
            }
        }

        if ((tac & 4) != 0) {
            switch (tac & 3) {
            case 0:
                if (Clock.main >= 64)
                    step(mmu);
                break;
            case 1:
                if (Clock.main >= 1)
                    step(mmu);
                break;
            case 2:
                if (Clock.main >= 4)
                    step(mmu);
                break;
            case 3:
                if (Clock.main >= 16)
                    step(mmu);
                break;
            }
        }
    }

    public int rb(int addr) {
        switch (addr) {
        case 0xFF04:
            return div;
        case 0xFF05:
            return tima;
        case 0xFF06:
            return tma;
        case 0xFF07:
            return tac;
        }

        return 0;
    }

    public void wb(int addr, int val) {
        switch (addr) {
        case 0xFF04:
            div = 0;
            break;
        case 0xFF05:
            tima = val;
            break;
        case 0xFF06:
            tma = val;
            break;
        case 0xFF07:
            tac = val&7;
            break;
        }
    }
}