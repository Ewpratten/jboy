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
        int t = 0;
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
        ops.mmu = mmu;
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
        System.out.println("Reset emulator");

        ops.r = r;
        ops.rsv = rsv;
        ops.clock = clock;
    }

    String[] map = new String[] { // 00
            "NOP", "LDBCnn", "LDBCmA", "INCBC", "INCr_b", "DECr_b", "LDrn_b", "RLCA", "LDmmSP", "ADDHLBC", "LDABCm",
            "DECBC", "INCr_c", "DECr_c", "LDrn_c", "RRCA", "DJNZn", "LDDEnn", "LDDEmA", "INCDE", "INCr_d", "DECr_d",
            "LDrn_d", "RLA", "JRn", "ADDHLDE", "LDADEm", "DECDE", "INCr_e", "DECr_e", "LDrn_e", "RRA", "JRNZn",
            "LDHLnn", "LDHLIA", "INCHL", "INCr_h", "DECr_h", "LDrn_h", "DAA", "JRZn", "ADDHLHL", "LDAHLI", "DECHL",
            "INCr_l", "DECr_l", "LDrn_l", "CPL", "JRNCn", "LDSPnn", "LDHLDA", "INCSP", "INCHLm", "DECHLm", "LDHLmn",
            "SCF", "JRCn", "ADDHLSP", "LDAHLD", "DECSP", "INCr_a", "DECr_a", "LDrn_a", "CCF", "LDrr_bb", "LDrr_bc",
            "LDrr_bd", "LDrr_be", "LDrr_bh", "LDrr_bl", "LDrHLm_b", "LDrr_ba", "LDrr_cb", "LDrr_cc", "LDrr_cd",
            "LDrr_ce", "LDrr_ch", "LDrr_cl", "LDrHLm_c", "LDrr_ca", "LDrr_db", "LDrr_dc", "LDrr_dd", "LDrr_de",
            "LDrr_dh", "LDrr_dl", "LDrHLm_d", "LDrr_da", "LDrr_eb", "LDrr_ec", "LDrr_ed", "LDrr_ee", "LDrr_eh",
            "LDrr_el", "LDrHLm_e", "LDrr_ea", "LDrr_hb", "LDrr_hc", "LDrr_hd", "LDrr_he", "LDrr_hh", "LDrr_hl",
            "LDrHLm_h", "LDrr_ha", "LDrr_lb", "LDrr_lc", "LDrr_ld", "LDrr_le", "LDrr_lh", "LDrr_ll", "LDrHLm_l",
            "LDrr_la", "LDHLmr_b", "LDHLmr_c", "LDHLmr_d", "LDHLmr_e", "LDHLmr_h", "LDHLmr_l", "HALT", "LDHLmr_a",
            "LDrr_ab", "LDrr_ac", "LDrr_ad", "LDrr_ae", "LDrr_ah", "LDrr_al", "LDrHLm_a", "LDrr_aa", "ADDr_b", "ADDr_c",
            "ADDr_d", "ADDr_e", "ADDr_h", "ADDr_l", "ADDHL", "ADDr_a", "ADCr_b", "ADCr_c", "ADCr_d", "ADCr_e", "ADCr_h",
            "ADCr_l", "ADCHL", "ADCr_a", "SUBr_b", "SUBr_c", "SUBr_d", "SUBr_e", "SUBr_h", "SUBr_l", "SUBHL", "SUBr_a",
            "SBCr_b", "SBCr_c", "SBCr_d", "SBCr_e", "SBCr_h", "SBCr_l", "SBCHL", "SBCr_a", "ANDr_b", "ANDr_c", "ANDr_d",
            "ANDr_e", "ANDr_h", "ANDr_l", "ANDHL", "ANDr_a", "XORr_b", "XORr_c", "XORr_d", "XORr_e", "XORr_h", "XORr_l",
            "XORHL", "XORr_a", "ORr_b", "ORr_c", "ORr_d", "ORr_e", "ORr_h", "ORr_l", "ORHL", "ORr_a", "CPr_b", "CPr_c",
            "CPr_d", "CPr_e", "CPr_h", "CPr_l", "CPHL", "CPr_a", "RETNZ", "POPBC", "JPNZnn", "JPnn", "CALLNZnn",
            "PUSHBC", "ADDn", "RST00", "RETZ", "RET", "JPZnn", "MAPcb", "CALLZnn", "CALLnn", "ADCn", "RST08", "RETNC",
            "POPDE", "JPNCnn", "XX", "CALLNCnn", "PUSHDE", "SUBn", "RST10", "RETC", "RETI", "JPCnn", "XX", "CALLCnn",
            "XX", "SBCn", "RST18", "LDIOnA", "POPHL", "LDIOCA", "XX", "XX", "PUSHHL", "ANDn", "RST20", "ADDSPn", "JPHL",
            "LDmmA", "XX", "XX", "XX", "XORn", "RST28", "LDAIOn", "POPAF", "LDAIOC", "DI", "XX", "PUSHAF", "ORn",
            "RST30", "LDHLSPn", "XX", "LDAmm", "EI", "XX", "XX", "CPn", "RST38" };

    String[] cbmap = new String[] { "RLCr_b", "RLCr_c", "RLCr_d", "RLCr_e", "RLCr_h", "RLCr_l", "RLCHL", "RLCr_a",
            "RRCr_b", "RRCr_c", "RRCr_d", "RRCr_e", "RRCr_h", "RRCr_l", "RRCHL", "RRCr_a", "RLr_b", "RLr_c", "RLr_d",
            "RLr_e", "RLr_h", "RLr_l", "RLHL", "RLr_a", "RRr_b", "RRr_c", "RRr_d", "RRr_e", "RRr_h", "RRr_l", "RRHL",
            "RRr_a", "SLAr_b", "SLAr_c", "SLAr_d", "SLAr_e", "SLAr_h", "SLAr_l", "XX", "SLAr_a", "SRAr_b", "SRAr_c",
            "SRAr_d", "SRAr_e", "SRAr_h", "SRAr_l", "XX", "SRAr_a", "SWAPr_b", "SWAPr_c", "SWAPr_d", "SWAPr_e",
            "SWAPr_h", "SWAPr_l", "XX", "SWAPr_a", "SRLr_b", "SRLr_c", "SRLr_d", "SRLr_e", "SRLr_h", "SRLr_l", "XX",
            "SRLr_a", "BIT0b", "BIT0c", "BIT0d", "BIT0e", "BIT0h", "BIT0l", "BIT0m", "BIT0a", "BIT1b", "BIT1c", "BIT1d",
            "BIT1e", "BIT1h", "BIT1l", "BIT1m", "BIT1a", "BIT2b", "BIT2c", "BIT2d", "BIT2e", "BIT2h", "BIT2l", "BIT2m",
            "BIT2a", "BIT3b", "BIT3c", "BIT3d", "BIT3e", "BIT3h", "BIT3l", "BIT3m", "BIT3a", "BIT4b", "BIT4c", "BIT4d",
            "BIT4e", "BIT4h", "BIT4l", "BIT4m", "BIT4a", "BIT5b", "BIT5c", "BIT5d", "BIT5e", "BIT5h", "BIT5l", "BIT5m",
            "BIT5a", "BIT6b", "BIT6c", "BIT6d", "BIT6e", "BIT6h", "BIT6l", "BIT6m", "BIT6a", "BIT7b", "BIT7c", "BIT7d",
            "BIT7e", "BIT7h", "BIT7l", "BIT7m", "BIT7a", "RES0b", "RES0c", "RES0d", "RES0e", "RES0h", "RES0l", "RES0m",
            "RES0a", "RES1b", "RES1c", "RES1d", "RES1e", "RES1h", "RES1l", "RES1m", "RES1a", "RES2b", "RES2c", "RES2d",
            "RES2e", "RES2h", "RES2l", "RES2m", "RES2a", "RES3b", "RES3c", "RES3d", "RES3e", "RES3h", "RES3l", "RES3m",
            "RES3a", "RES4b", "RES4c", "RES4d", "RES4e", "RES4h", "RES4l", "RES4m", "RES4a", "RES5b", "RES5c", "RES5d",
            "RES5e", "RES5h", "RES5l", "RES5m", "RES5a", "RES6b", "RES6c", "RES6d", "RES6e", "RES6h", "RES6l", "RES6m",
            "RES6a", "RES7b", "RES7c", "RES7d", "RES7e", "RES7h", "RES7l", "RES7m", "RES7a", "SET0b", "SET0c", "SET0d",
            "SET0e", "SET0h", "SET0l", "SET0m", "SET0a", "SET1b", "SET1c", "SET1d", "SET1e", "SET1h", "SET1l", "SET1m",
            "SET1a", "SET2b", "SET2c", "SET2d", "SET2e", "SET2h", "SET2l", "SET2m", "SET2a", "SET3b", "SET3c", "SET3d",
            "SET3e", "SET3h", "SET3l", "SET3m", "SET3a", "SET4b", "SET4c", "SET4d", "SET4e", "SET4h", "SET4l", "SET4m",
            "SET4a", "SET5b", "SET5c", "SET5d", "SET5e", "SET5h", "SET5l", "SET5m", "SET5a", "SET6b", "SET6c", "SET6d",
            "SET6e", "SET6h", "SET6l", "SET6m", "SET6a", "SET7b", "SET7c", "SET7d", "SET7e", "SET7h", "SET7l", "SET7m",
            "SET7a" };

    public void exec(int instruction) {
        // r.r = (r.r + 1) & 127;

        /* Im so sorry... */
        // int ins = (mmu.rb(r.pc++));
        if (instruction >= map.length) {
            System.out.println("Unknown instruction: " + instruction);
        } else {
            try {
                ops.getClass().getMethod(map[instruction]).invoke(ops);
            } catch (Exception e) {
                // TODO: handle exception
                System.out.println("Failed to execute instruction: " + instruction);
            }
        }
    }

    public class OPS {
        MMU mmu;
        Registers r;
        RSV rsv;
        Clock clock;


        /*--- Load/store ---*/
        public void LDrr_bb() {
            r.b = r.b;
            r.m = 1;
        }

        public void LDrr_bc() {
            r.b = r.c;
            r.m = 1;
        }

        public void LDrr_bd() {
            r.b = r.d;
            r.m = 1;
        }

        public void LDrr_be() {
            r.b = r.e;
            r.m = 1;
        }

        public void LDrr_bh() {
            r.b = r.h;
            r.m = 1;
        }

        public void LDrr_bl() {
            r.b = r.l;
            r.m = 1;
        }

        public void LDrr_ba() {
            r.b = r.a;
            r.m = 1;
        }

        public void LDrr_cb() {
            r.c = r.b;
            r.m = 1;
        }

        public void LDrr_cc() {
            r.c = r.c;
            r.m = 1;
        }

        public void LDrr_cd() {
            r.c = r.d;
            r.m = 1;
        }

        public void LDrr_ce() {
            r.c = r.e;
            r.m = 1;
        }

        public void LDrr_ch() {
            r.c = r.h;
            r.m = 1;
        }

        public void LDrr_cl() {
            r.c = r.l;
            r.m = 1;
        }

        public void LDrr_ca() {
            r.c = r.a;
            r.m = 1;
        }

        public void LDrr_db() {
            r.d = r.b;
            r.m = 1;
        }

        public void LDrr_dc() {
            r.d = r.c;
            r.m = 1;
        }

        public void LDrr_dd() {
            r.d = r.d;
            r.m = 1;
        }

        public void LDrr_de() {
            r.d = r.e;
            r.m = 1;
        }

        public void LDrr_dh() {
            r.d = r.h;
            r.m = 1;
        }

        public void LDrr_dl() {
            r.d = r.l;
            r.m = 1;
        }

        public void LDrr_da() {
            r.d = r.a;
            r.m = 1;
        }

        public void LDrr_eb() {
            r.e = r.b;
            r.m = 1;
        }

        public void LDrr_ec() {
            r.e = r.c;
            r.m = 1;
        }

        public void LDrr_ed() {
            r.e = r.d;
            r.m = 1;
        }

        public void LDrr_ee() {
            r.e = r.e;
            r.m = 1;
        }

        public void LDrr_eh() {
            r.e = r.h;
            r.m = 1;
        }

        public void LDrr_el() {
            r.e = r.l;
            r.m = 1;
        }

        public void LDrr_ea() {
            r.e = r.a;
            r.m = 1;
        }

        public void LDrr_hb() {
            r.h = r.b;
            r.m = 1;
        }

        public void LDrr_hc() {
            r.h = r.c;
            r.m = 1;
        }

        public void LDrr_hd() {
            r.h = r.d;
            r.m = 1;
        }

        public void LDrr_he() {
            r.h = r.e;
            r.m = 1;
        }

        public void LDrr_hh() {
            r.h = r.h;
            r.m = 1;
        }

        public void LDrr_hl() {
            r.h = r.l;
            r.m = 1;
        }

        public void LDrr_ha() {
            r.h = r.a;
            r.m = 1;
        }

        public void LDrr_lb() {
            r.l = r.b;
            r.m = 1;
        }

        public void LDrr_lc() {
            r.l = r.c;
            r.m = 1;
        }

        public void LDrr_ld() {
            r.l = r.d;
            r.m = 1;
        }

        public void LDrr_le() {
            r.l = r.e;
            r.m = 1;
        }

        public void LDrr_lh() {
            r.l = r.h;
            r.m = 1;
        }

        public void LDrr_ll() {
            r.l = r.l;
            r.m = 1;
        }

        public void LDrr_la() {
            r.l = r.a;
            r.m = 1;
        }

        public void LDrr_ab() {
            r.a = r.b;
            r.m = 1;
        }

        public void LDrr_ac() {
            r.a = r.c;
            r.m = 1;
        }

        public void LDrr_ad() {
            r.a = r.d;
            r.m = 1;
        }

        public void LDrr_ae() {
            r.a = r.e;
            r.m = 1;
        }

        public void LDrr_ah() {
            r.a = r.h;
            r.m = 1;
        }

        public void LDrr_al() {
            r.a = r.l;
            r.m = 1;
        }

        public void LDrr_aa() {
            r.a = r.a;
            r.m = 1;
        }

        public void LDrHLm_b() {
            r.b = mmu.rb((r.h << 8) + r.l);
            r.m = 2;
        }

        public void LDrHLm_c() {
            r.c = mmu.rb((r.h << 8) + r.l);
            r.m = 2;
        }

        public void LDrHLm_d() {
            r.d = mmu.rb((r.h << 8) + r.l);
            r.m = 2;
        }

        public void LDrHLm_e() {
            r.e = mmu.rb((r.h << 8) + r.l);
            r.m = 2;
        }

        public void LDrHLm_h() {
            r.h = mmu.rb((r.h << 8) + r.l);
            r.m = 2;
        }

        public void LDrHLm_l() {
            r.l = mmu.rb((r.h << 8) + r.l);
            r.m = 2;
        }

        public void LDrHLm_a() {
            r.a = mmu.rb((r.h << 8) + r.l);
            r.m = 2;
        }

        public void LDHLmr_b() {
            mmu.wb((r.h << 8) + r.l, r.b);
            r.m = 2;
        }

        public void LDHLmr_c() {
            mmu.wb((r.h << 8) + r.l, r.c);
            r.m = 2;
        }

        public void LDHLmr_d() {
            mmu.wb((r.h << 8) + r.l, r.d);
            r.m = 2;
        }

        public void LDHLmr_e() {
            mmu.wb((r.h << 8) + r.l, r.e);
            r.m = 2;
        }

        public void LDHLmr_h() {
            mmu.wb((r.h << 8) + r.l, r.h);
            r.m = 2;
        }

        public void LDHLmr_l() {
            mmu.wb((r.h << 8) + r.l, r.l);
            r.m = 2;
        }

        public void LDHLmr_a() {
            mmu.wb((r.h << 8) + r.l, r.a);
            r.m = 2;
        }

        public void LDrn_b() {
            r.b = mmu.rb(r.pc);
            r.pc++;
            r.m = 2;
        }

        public void LDrn_c() {
            r.c = mmu.rb(r.pc);
            r.pc++;
            r.m = 2;
        }

        public void LDrn_d() {
            r.d = mmu.rb(r.pc);
            r.pc++;
            r.m = 2;
        }

        public void LDrn_e() {
            r.e = mmu.rb(r.pc);
            r.pc++;
            r.m = 2;
        }

        public void LDrn_h() {
            r.h = mmu.rb(r.pc);
            r.pc++;
            r.m = 2;
        }

        public void LDrn_l() {
            r.l = mmu.rb(r.pc);
            r.pc++;
            r.m = 2;
        }

        public void LDrn_a() {
            r.a = mmu.rb(r.pc);
            r.pc++;
            r.m = 2;
        }

        public void LDHLmn() {
            mmu.wb((r.h << 8) + r.l, mmu.rb(r.pc));
            r.pc++;
            r.m = 3;
        }

        public void LDBCmA() {
            mmu.wb((r.b << 8) + r.c, r.a);
            r.m = 2;
        }

        public void LDDEmA() {
            mmu.wb((r.d << 8) + r.e, r.a);
            r.m = 2;
        }

        public void LDmmA() {
            mmu.wb(mmu.rw(r.pc), r.a);
            r.pc += 2;
            r.m = 4;
        }

        public void LDABCm() {
            r.a = mmu.rb((r.b << 8) + r.c);
            r.m = 2;
        }

        public void LDADEm() {
            r.a = mmu.rb((r.d << 8) + r.e);
            r.m = 2;
        }

        public void LDAmm() {
            r.a = mmu.rb(mmu.rw(r.pc));
            r.pc += 2;
            r.m = 4;
        }

        public void LDBCnn() {
            r.c = mmu.rb(r.pc);
            r.b = mmu.rb(r.pc + 1);
            r.pc += 2;
            r.m = 3;
        }

        public void LDDEnn() {
            r.e = mmu.rb(r.pc);
            r.d = mmu.rb(r.pc + 1);
            r.pc += 2;
            r.m = 3;
        }

        public void LDHLnn() {
            r.l = mmu.rb(r.pc);
            r.h = mmu.rb(r.pc + 1);
            r.pc += 2;
            r.m = 3;
        }

        public void LDSPnn() {
            r.sp = mmu.rw(r.pc);
            r.pc += 2;
            r.m = 3;
        }

        public void LDHLmm() {
            int i = mmu.rw(r.pc);
            r.pc += 2;
            r.l = mmu.rb((i));
            r.h = mmu.rb(i + 1);
            r.m = 5;
        }

        public void LDmmHL() {
            int i = mmu.rw(r.pc);
            r.pc += 2;
            mmu.ww(i, (r.h << 8) + r.l);
            r.m = 5;
        }

        public void LDHLIA() {
            mmu.wb((r.h << 8) + r.l, r.a);
            r.l = (r.l + 1) & 255;
            if (r.l == 0)
                r.h = (r.h + 1) & 255;
            r.m = 2;
        }

        public void LDAHLI() {
            r.a = mmu.rb((r.h << 8) + r.l);
            r.l = (r.l + 1) & 255;
            if (r.l == 0)
                r.h = (r.h + 1) & 255;
            r.m = 2;
        }

        public void LDHLDA() {
            mmu.wb((r.h << 8) + r.l, r.a);
            r.l = (r.l - 1) & 255;
            if (r.l == 255)
                r.h = (r.h - 1) & 255;
            r.m = 2;
        }

        public void LDAHLD() {
            r.a = mmu.rb((r.h << 8) + r.l);
            r.l = (r.l - 1) & 255;
            if (r.l == 255)
                r.h = (r.h - 1) & 255;
            r.m = 2;
        }

        public void LDAIOn() {
            r.a = mmu.rb(0xFF00 + mmu.rb(r.pc));
            r.pc++;
            r.m = 3;
        }

        public void LDIOnA() {
            mmu.wb(0xFF00 + mmu.rb(r.pc), r.a);
            r.pc++;
            r.m = 3;
        }

        public void LDAIOC() {
            r.a = mmu.rb(0xFF00 + r.c);
            r.m = 2;
        }

        public void LDIOCA() {
            mmu.wb(0xFF00 + r.c, r.a);
            r.m = 2;
        }

        public void LDHLSPn() {
            int i = mmu.rb(r.pc);
            if (i > 127)
                i = -((~i + 1) & 255);
            r.pc++;
            i += r.sp;
            r.h = (i >> 8) & 255;
            r.l = i & 255;
            r.m = 3;
        }

        public void SWAPr_b() {
            int tr = r.b;
            r.b = ((tr & 0xF) << 4) | ((tr & 0xF0) >> 4);
            r.f = (r.b != 0) ? 0 : 0x80;
            r.m = 1;
        }

        public void SWAPr_c() {
            int tr = r.c;
            r.c = ((tr & 0xF) << 4) | ((tr & 0xF0) >> 4);
            r.f = (r.c != 0) ? 0 : 0x80;
            r.m = 1;
        }

        public void SWAPr_d() {
            int tr = r.d;
            r.d = ((tr & 0xF) << 4) | ((tr & 0xF0) >> 4);
            r.f = (r.d != 0) ? 0 : 0x80;
            r.m = 1;
        }

        public void SWAPr_e() {
            int tr = r.e;
            r.e = ((tr & 0xF) << 4) | ((tr & 0xF0) >> 4);
            r.f = (r.e != 0) ? 0 : 0x80;
            r.m = 1;
        }

        public void SWAPr_h() {
            int tr = r.h;
            r.h = ((tr & 0xF) << 4) | ((tr & 0xF0) >> 4);
            r.f = (r.h != 0) ? 0 : 0x80;
            r.m = 1;
        }

        public void SWAPr_l() {
            int tr = r.l;
            r.l = ((tr & 0xF) << 4) | ((tr & 0xF0) >> 4);
            r.f = (r.l != 0) ? 0 : 0x80;
            r.m = 1;
        }

        public void SWAPr_a() {
            int tr = r.a;
            r.a = ((tr & 0xF) << 4) | ((tr & 0xF0) >> 4);
            r.f = (r.a != 0) ? 0 : 0x80;
            r.m = 1;
        }

        /*--- Data processing ---*/
        public void ADDr_b() {
            int a = r.a;
            r.a += r.b;
            r.f = (r.a > 255) ? 0x10 : 0;
            r.a &= 255;
            if (r.a == 0)
                r.f |= 0x80;
            if (((r.a ^ r.b ^ a) & 0x10) != 0)
                r.f |= 0x20;
            r.m = 1;
        }

        public void ADDr_c() {
            int a = r.a;
            r.a += r.c;
            r.f = (r.a > 255) ? 0x10 : 0;
            r.a &= 255;
            if (r.a == 0)
                r.f |= 0x80;
            if (((r.a ^ r.c ^ a) & 0x10) != 0)
                r.f |= 0x20;
            r.m = 1;
        }

        public void ADDr_d() {
            int a = r.a;
            r.a += r.d;
            r.f = (r.a > 255) ? 0x10 : 0;
            r.a &= 255;
            if (r.a == 0)
                r.f |= 0x80;
            if (((r.a ^ r.d ^ a) & 0x10) != 0)
                r.f |= 0x20;
            r.m = 1;
        }

        public void ADDr_e() {
            int a = r.a;
            r.a += r.e;
            r.f = (r.a > 255) ? 0x10 : 0;
            r.a &= 255;
            if (r.a == 0)
                r.f |= 0x80;
            if (((r.a ^ r.e ^ a) & 0x10) != 0)
                r.f |= 0x20;
            r.m = 1;
        }

        public void ADDr_h() {
            int a = r.a;
            r.a += r.h;
            r.f = (r.a > 255) ? 0x10 : 0;
            r.a &= 255;
            if (r.a == 0)
                r.f |= 0x80;
            if (((r.a ^ r.h ^ a) & 0x10) != 0)
                r.f |= 0x20;
            r.m = 1;
        }

        public void ADDr_l() {
            int a = r.a;
            r.a += r.l;
            r.f = (r.a > 255) ? 0x10 : 0;
            r.a &= 255;
            if (r.a == 0)
                r.f |= 0x80;
            if (((r.a ^ r.l ^ a) & 0x10) != 0)
                r.f |= 0x20;
            r.m = 1;
        }

        public void ADDr_a() {
            int a = r.a;
            r.a += r.a;
            r.f = (r.a > 255) ? 0x10 : 0;
            r.a &= 255;
            if (r.a == 0)
                r.f |= 0x80;
            if (((r.a ^ r.a ^ a) & 0x10) != 0)
                r.f |= 0x20;
            r.m = 1;
        }

        public void ADDHL() {
            int a = r.a;
            int m = mmu.rb((r.h << 8) + r.l);
            r.a += m;
            r.f = (r.a > 255) ? 0x10 : 0;
            r.a &= 255;
            if (r.a == 0)
                r.f |= 0x80;
            if (((r.a ^ a ^ m) & 0x10) != 0)
                r.f |= 0x20;
            r.m = 2;
        }

        public void ADDn() {
            int a = r.a;
            int m = mmu.rb(r.pc);
            r.a += m;
            r.pc++;
            r.f = (r.a > 255) ? 0x10 : 0;
            r.a &= 255;
            if (r.a == 0)
                r.f |= 0x80;
            if (((r.a ^ a ^ m) & 0x10) != 0)
                r.f |= 0x20;
            r.m = 2;
        }

        public void ADDHLBC() {
            int hl = (r.h << 8) + r.l;
            hl += (r.b << 8) + r.c;
            if (hl > 65535)
                r.f |= 0x10;
            else
                r.f &= 0xEF;
            r.h = (hl >> 8) & 255;
            r.l = hl & 255;
            r.m = 3;
        }

        public void ADDHLDE() {
            int hl = (r.h << 8) + r.l;
            hl += (r.d << 8) + r.e;
            if (hl > 65535)
                r.f |= 0x10;
            else
                r.f &= 0xEF;
            r.h = (hl >> 8) & 255;
            r.l = hl & 255;
            r.m = 3;
        }

        public void ADDHLHL() {
            int hl = (r.h << 8) + r.l;
            hl += (r.h << 8) + r.l;
            if (hl > 65535)
                r.f |= 0x10;
            else
                r.f &= 0xEF;
            r.h = (hl >> 8) & 255;
            r.l = hl & 255;
            r.m = 3;
        }

        public void ADDHLSP() {
            int hl = (r.h << 8) + r.l;
            hl += r.sp;
            if (hl > 65535)
                r.f |= 0x10;
            else
                r.f &= 0xEF;
            r.h = (hl >> 8) & 255;
            r.l = hl & 255;
            r.m = 3;
        }

        public void ADDSPn() {
            int i = mmu.rb(r.pc);
            if (i > 127)
                i = -((~i + 1) & 255);
            r.pc++;
            r.sp += i;
            r.m = 4;
        }

        public void ADCr_b() {
            int a = r.a;
            r.a += r.b;
            r.a += ((((r.f & 0x10) != 0))) ? 1 : 0;
            r.f = (r.a > 255) ? 0x10 : 0;
            r.a &= 255;
            if (r.a == 0)
                r.f |= 0x80;
            if (((r.a ^ r.b ^ a) & 0x10) != 0)
                r.f |= 0x20;
            r.m = 1;
        }

        public void ADCr_c() {
            int a = r.a;
            r.a += r.c;
            r.a += ((((r.f & 0x10) != 0))) ? 1 : 0;
            r.f = (r.a > 255) ? 0x10 : 0;
            r.a &= 255;
            if (r.a == 0)
                r.f |= 0x80;
            if (((r.a ^ r.c ^ a) & 0x10) != 0)
                r.f |= 0x20;
            r.m = 1;
        }

        public void ADCr_d() {
            int a = r.a;
            r.a += r.d;
            r.a += ((((r.f & 0x10) != 0))) ? 1 : 0;
            r.f = (r.a > 255) ? 0x10 : 0;
            r.a &= 255;
            if (r.a == 0)
                r.f |= 0x80;
            if (((r.a ^ r.d ^ a) & 0x10) != 0)
                r.f |= 0x20;
            r.m = 1;
        }

        public void ADCr_e() {
            int a = r.a;
            r.a += r.e;
            r.a += ((((r.f & 0x10) != 0))) ? 1 : 0;
            r.f = (r.a > 255) ? 0x10 : 0;
            r.a &= 255;
            if (r.a == 0)
                r.f |= 0x80;
            if (((r.a ^ r.e ^ a) & 0x10) != 0)
                r.f |= 0x20;
            r.m = 1;
        }

        public void ADCr_h() {
            int a = r.a;
            r.a += r.h;
            r.a += ((((r.f & 0x10) != 0))) ? 1 : 0;
            r.f = (r.a > 255) ? 0x10 : 0;
            r.a &= 255;
            if (r.a == 0)
                r.f |= 0x80;
            if (((r.a ^ r.h ^ a) & 0x10) != 0)
                r.f |= 0x20;
            r.m = 1;
        }

        public void ADCr_l() {
            int a = r.a;
            r.a += r.l;
            r.a += ((((r.f & 0x10) != 0))) ? 1 : 0;
            r.f = (r.a > 255) ? 0x10 : 0;
            r.a &= 255;
            if (r.a == 0)
                r.f |= 0x80;
            if (((r.a ^ r.l ^ a) & 0x10) != 0)
                r.f |= 0x20;
            r.m = 1;
        }

        public void ADCr_a() {
            int a = r.a;
            r.a += r.a;
            r.a += ((((r.f & 0x10) != 0))) ? 1 : 0;
            r.f = (r.a > 255) ? 0x10 : 0;
            r.a &= 255;
            if (r.a == 0)
                r.f |= 0x80;
            if (((r.a ^ r.a ^ a) & 0x10) != 0)
                r.f |= 0x20;
            r.m = 1;
        }

        public void ADCHL() {
            int a = r.a;
            int m = mmu.rb((r.h << 8) + r.l);
            r.a += m;
            r.a += ((r.f & 0x10) != 0) ? 1 : 0;
            r.f = (r.a > 255) ? 0x10 : 0;
            r.a &= 255;
            if (r.a == 0)
                r.f |= 0x80;
            if (((r.a ^ m ^ a) & 0x10) != 0)
                r.f |= 0x20;
            r.m = 2;
        }

        public void ADCn() {
            int a = r.a;
            int m = mmu.rb(r.pc);
            r.a += m;
            r.pc++;
            r.a += ((r.f & 0x10) != 0) ? 1 : 0;
            r.f = (r.a > 255) ? 0x10 : 0;
            r.a &= 255;
            if (r.a == 0)
                r.f |= 0x80;
            if (((r.a ^ m ^ a) & 0x10) != 0)
                r.f |= 0x20;
            r.m = 2;
        }

        public void SUBr_b() {
            int a = r.a;
            r.a -= r.b;
            r.f = (r.a < 0) ? 0x50 : 0x40;
            r.a &= 255;
            if (r.a == 0)
                r.f |= 0x80;
            if (((r.a ^ r.b ^ a) & 0x10) != 0)
                r.f |= 0x20;
            r.m = 1;
        }

        public void SUBr_c() {
            int a = r.a;
            r.a -= r.c;
            r.f = (r.a < 0) ? 0x50 : 0x40;
            r.a &= 255;
            if (r.a == 0)
                r.f |= 0x80;
            if (((r.a ^ r.c ^ a) & 0x10) != 0)
                r.f |= 0x20;
            r.m = 1;
        }

        public void SUBr_d() {
            int a = r.a;
            r.a -= r.d;
            r.f = (r.a < 0) ? 0x50 : 0x40;
            r.a &= 255;
            if (r.a == 0)
                r.f |= 0x80;
            if (((r.a ^ r.d ^ a) & 0x10) != 0)
                r.f |= 0x20;
            r.m = 1;
        }

        public void SUBr_e() {
            int a = r.a;
            r.a -= r.e;
            r.f = (r.a < 0) ? 0x50 : 0x40;
            r.a &= 255;
            if (r.a == 0)
                r.f |= 0x80;
            if (((r.a ^ r.e ^ a) & 0x10) != 0)
                r.f |= 0x20;
            r.m = 1;
        }

        public void SUBr_h() {
            int a = r.a;
            r.a -= r.h;
            r.f = (r.a < 0) ? 0x50 : 0x40;
            r.a &= 255;
            if (r.a == 0)
                r.f |= 0x80;
            if (((r.a ^ r.h ^ a) & 0x10) != 0)
                r.f |= 0x20;
            r.m = 1;
        }

        public void SUBr_l() {
            int a = r.a;
            r.a -= r.l;
            r.f = (r.a < 0) ? 0x50 : 0x40;
            r.a &= 255;
            if (r.a == 0)
                r.f |= 0x80;
            if (((r.a ^ r.l ^ a) & 0x10) != 0)
                r.f |= 0x20;
            r.m = 1;
        }

        public void SUBr_a() {
            int a = r.a;
            r.a -= r.a;
            r.f = (r.a < 0) ? 0x50 : 0x40;
            r.a &= 255;
            if (r.a == 0)
                r.f |= 0x80;
            if (((r.a ^ r.a ^ a) & 0x10) != 0)
                r.f |= 0x20;
            r.m = 1;
        }

        public void SUBHL() {
            int a = r.a;
            int m = mmu.rb((r.h << 8) + r.l);
            r.a -= m;
            r.f = (r.a < 0) ? 0x50 : 0x40;
            r.a &= 255;
            if (r.a == 0)
                r.f |= 0x80;
            if (((r.a ^ m ^ a) & 0x10) != 0)
                r.f |= 0x20;
            r.m = 2;
        }

        public void SUBn() {
            int a = r.a;
            int m = mmu.rb(r.pc);
            r.a -= m;
            r.pc++;
            r.f = (r.a < 0) ? 0x50 : 0x40;
            r.a &= 255;
            if (r.a == 0)
                r.f |= 0x80;
            if (((r.a ^ m ^ a) & 0x10) != 0)
                r.f |= 0x20;
            r.m = 2;
        }

        public void SBCr_b() {
            int a = r.a;
            r.a -= r.b;
            r.a -= ((((r.f & 0x10) != 0))) ? 1 : 0;
            r.f = (r.a < 0) ? 0x50 : 0x40;
            r.a &= 255;
            if (r.a == 0)
                r.f |= 0x80;
            if (((r.a ^ r.b ^ a) & 0x10) != 0)
                r.f |= 0x20;
            r.m = 1;
        }

        public void SBCr_c() {
            int a = r.a;
            r.a -= r.c;
            r.a -= ((((r.f & 0x10) != 0))) ? 1 : 0;
            r.f = (r.a < 0) ? 0x50 : 0x40;
            r.a &= 255;
            if (r.a == 0)
                r.f |= 0x80;
            if (((r.a ^ r.c ^ a) & 0x10) != 0)
                r.f |= 0x20;
            r.m = 1;
        }

        public void SBCr_d() {
            int a = r.a;
            r.a -= r.d;
            r.a -= ((((r.f & 0x10) != 0))) ? 1 : 0;
            r.f = (r.a < 0) ? 0x50 : 0x40;
            r.a &= 255;
            if (r.a == 0)
                r.f |= 0x80;
            if (((r.a ^ r.d ^ a) & 0x10) != 0)
                r.f |= 0x20;
            r.m = 1;
        }

        public void SBCr_e() {
            int a = r.a;
            r.a -= r.e;
            r.a -= ((((r.f & 0x10) != 0))) ? 1 : 0;
            r.f = (r.a < 0) ? 0x50 : 0x40;
            r.a &= 255;
            if (r.a == 0)
                r.f |= 0x80;
            if (((r.a ^ r.e ^ a) & 0x10) != 0)
                r.f |= 0x20;
            r.m = 1;
        }

        public void SBCr_h() {
            int a = r.a;
            r.a -= r.h;
            r.a -= ((((r.f & 0x10) != 0))) ? 1 : 0;
            r.f = (r.a < 0) ? 0x50 : 0x40;
            r.a &= 255;
            if (r.a == 0)
                r.f |= 0x80;
            if (((r.a ^ r.h ^ a) & 0x10) != 0)
                r.f |= 0x20;
            r.m = 1;
        }

        public void SBCr_l() {
            int a = r.a;
            r.a -= r.l;
            r.a -= ((((r.f & 0x10) != 0))) ? 1 : 0;
            r.f = (r.a < 0) ? 0x50 : 0x40;
            r.a &= 255;
            if (r.a == 0)
                r.f |= 0x80;
            if (((r.a ^ r.l ^ a) & 0x10) != 0)
                r.f |= 0x20;
            r.m = 1;
        }

        public void SBCr_a() {
            int a = r.a;
            r.a -= r.a;
            r.a -= ((((r.f & 0x10) != 0))) ? 1 : 0;
            r.f = (r.a < 0) ? 0x50 : 0x40;
            r.a &= 255;
            if (r.a == 0)
                r.f |= 0x80;
            if (((r.a ^ r.a ^ a) & 0x10) != 0)
                r.f |= 0x20;
            r.m = 1;
        }

        public void SBCHL() {
            int a = r.a;
            int m = mmu.rb((r.h << 8) + r.l);
            r.a -= m;
            r.a -= ((r.f & 0x10) != 0) ? 1 : 0;
            r.f = (r.a < 0) ? 0x50 : 0x40;
            r.a &= 255;
            if (r.a == 0)
                r.f |= 0x80;
            if (((r.a ^ m ^ a) & 0x10) != 0)
                r.f |= 0x20;
            r.m = 2;
        }

        public void SBCn() {
            int a = r.a;
            int m = mmu.rb(r.pc);
            r.a -= m;
            r.pc++;
            r.a -= ((r.f & 0x10) != 0) ? 1 : 0;
            r.f = (r.a < 0) ? 0x50 : 0x40;
            r.a &= 255;
            if (r.a == 0)
                r.f |= 0x80;
            if (((r.a ^ m ^ a) & 0x10) != 0)
                r.f |= 0x20;
            r.m = 2;
        }

        public void CPr_b() {
            int i = r.a;
            i -= r.b;
            r.f = (i < 0) ? 0x50 : 0x40;
            i &= 255;
            if (i != 0)
                r.f |= 0x80;
            if (((r.a ^ r.b ^ i) & 0x10) != 0)
                r.f |= 0x20;
            r.m = 1;
        }

        public void CPr_c() {
            int i = r.a;
            i -= r.c;
            r.f = (i < 0) ? 0x50 : 0x40;
            i &= 255;
            if (i != 0)
                r.f |= 0x80;
            if (((r.a ^ r.c ^ i) & 0x10) != 0)
                r.f |= 0x20;
            r.m = 1;
        }

        public void CPr_d() {
            int i = r.a;
            i -= r.d;
            r.f = (i < 0) ? 0x50 : 0x40;
            i &= 255;
            if (i != 0)
                r.f |= 0x80;
            if (((r.a ^ r.d ^ i) & 0x10) != 0)
                r.f |= 0x20;
            r.m = 1;
        }

        public void CPr_e() {
            int i = r.a;
            i -= r.e;
            r.f = (i < 0) ? 0x50 : 0x40;
            i &= 255;
            if (i != 0)
                r.f |= 0x80;
            if (((r.a ^ r.e ^ i) & 0x10) != 0)
                r.f |= 0x20;
            r.m = 1;
        }

        public void CPr_h() {
            int i = r.a;
            i -= r.h;
            r.f = (i < 0) ? 0x50 : 0x40;
            i &= 255;
            if (i != 0)
                r.f |= 0x80;
            if (((r.a ^ r.h ^ i) & 0x10) != 0)
                r.f |= 0x20;
            r.m = 1;
        }

        public void CPr_l() {
            int i = r.a;
            i -= r.l;
            r.f = (i < 0) ? 0x50 : 0x40;
            i &= 255;
            if (i != 0)
                r.f |= 0x80;
            if (((r.a ^ r.l ^ i) & 0x10) != 0)
                r.f |= 0x20;
            r.m = 1;
        }

        public void CPr_a() {
            int i = r.a;
            i -= r.a;
            r.f = (i < 0) ? 0x50 : 0x40;
            i &= 255;
            if (i != 0)
                r.f |= 0x80;
            if (((r.a ^ r.a ^ i) & 0x10) != 0)
                r.f |= 0x20;
            r.m = 1;
        }

        public void CPHL() {
            int i = r.a;
            int m = mmu.rb((r.h << 8) + r.l);
            i -= m;
            r.f = (i < 0) ? 0x50 : 0x40;
            i &= 255;
            if (i != 0)
                r.f |= 0x80;
            if (((r.a ^ i ^ m) & 0x10) != 0)
                r.f |= 0x20;
            r.m = 2;
        }

        public void CPn() {
            int i = r.a;
            int m = mmu.rb(r.pc);
            i -= m;
            r.pc++;
            r.f = (i < 0) ? 0x50 : 0x40;
            i &= 255;
            if (i != 0)
                r.f |= 0x80;
            if (((r.a ^ i ^ m) & 0x10) != 0)
                r.f |= 0x20;
            r.m = 2;
        }

        public void DAA() {
            int a = r.a;
            if ((r.f & 0x20) != 0 || ((r.a & 15) > 9))
                r.a += 6;
            r.f &= 0xEF;
            if ((r.f & 0x20) != 0 || (a > 0x99)) {
                r.a += 0x60;
                r.f |= 0x10;
            }
            r.m = 1;
        }

        public void ANDr_b() {
            r.a &= r.b;
            r.a &= 255;
            r.f = (r.a != 0) ? 0 : 0x80;
            r.m = 1;
        }

        public void ANDr_c() {
            r.a &= r.c;
            r.a &= 255;
            r.f = (r.a != 0) ? 0 : 0x80;
            r.m = 1;
        }

        public void ANDr_d() {
            r.a &= r.d;
            r.a &= 255;
            r.f = (r.a != 0) ? 0 : 0x80;
            r.m = 1;
        }

        public void ANDr_e() {
            r.a &= r.e;
            r.a &= 255;
            r.f = (r.a != 0) ? 0 : 0x80;
            r.m = 1;
        }

        public void ANDr_h() {
            r.a &= r.h;
            r.a &= 255;
            r.f = (r.a != 0) ? 0 : 0x80;
            r.m = 1;
        }

        public void ANDr_l() {
            r.a &= r.l;
            r.a &= 255;
            r.f = (r.a != 0) ? 0 : 0x80;
            r.m = 1;
        }

        public void ANDr_a() {
            r.a &= r.a;
            r.a &= 255;
            r.f = (r.a != 0) ? 0 : 0x80;
            r.m = 1;
        }

        public void ANDHL() {
            r.a &= mmu.rb((r.h << 8) + r.l);
            r.a &= 255;
            r.f = (r.a != 0) ? 0 : 0x80;
            r.m = 2;
        }

        public void ANDn() {
            r.a &= mmu.rb(r.pc);
            r.pc++;
            r.a &= 255;
            r.f = (r.a != 0) ? 0 : 0x80;
            r.m = 2;
        }

        public void ORr_b() {
            r.a |= r.b;
            r.a &= 255;
            r.f = (r.a != 0) ? 0 : 0x80;
            r.m = 1;
        }

        public void ORr_c() {
            r.a |= r.c;
            r.a &= 255;
            r.f = (r.a != 0) ? 0 : 0x80;
            r.m = 1;
        }

        public void ORr_d() {
            r.a |= r.d;
            r.a &= 255;
            r.f = (r.a != 0) ? 0 : 0x80;
            r.m = 1;
        }

        public void ORr_e() {
            r.a |= r.e;
            r.a &= 255;
            r.f = (r.a != 0) ? 0 : 0x80;
            r.m = 1;
        }

        public void ORr_h() {
            r.a |= r.h;
            r.a &= 255;
            r.f = (r.a != 0) ? 0 : 0x80;
            r.m = 1;
        }

        public void ORr_l() {
            r.a |= r.l;
            r.a &= 255;
            r.f = (r.a != 0) ? 0 : 0x80;
            r.m = 1;
        }

        public void ORr_a() {
            r.a |= r.a;
            r.a &= 255;
            r.f = (r.a != 0) ? 0 : 0x80;
            r.m = 1;
        }

        public void ORHL() {
            r.a |= mmu.rb((r.h << 8) + r.l);
            r.a &= 255;
            r.f = (r.a != 0) ? 0 : 0x80;
            r.m = 2;
        }

        public void ORn() {
            r.a |= mmu.rb(r.pc);
            r.pc++;
            r.a &= 255;
            r.f = (r.a != 0) ? 0 : 0x80;
            r.m = 2;
        }

        public void XORr_b() {
            r.a ^= r.b;
            r.a &= 255;
            r.f = (r.a != 0) ? 0 : 0x80;
            r.m = 1;
        }

        public void XORr_c() {
            r.a ^= r.c;
            r.a &= 255;
            r.f = (r.a != 0) ? 0 : 0x80;
            r.m = 1;
        }

        public void XORr_d() {
            r.a ^= r.d;
            r.a &= 255;
            r.f = (r.a != 0) ? 0 : 0x80;
            r.m = 1;
        }

        public void XORr_e() {
            r.a ^= r.e;
            r.a &= 255;
            r.f = (r.a != 0) ? 0 : 0x80;
            r.m = 1;
        }

        public void XORr_h() {
            r.a ^= r.h;
            r.a &= 255;
            r.f = (r.a != 0) ? 0 : 0x80;
            r.m = 1;
        }

        public void XORr_l() {
            r.a ^= r.l;
            r.a &= 255;
            r.f = (r.a != 0) ? 0 : 0x80;
            r.m = 1;
        }

        public void XORr_a() {
            r.a ^= r.a;
            r.a &= 255;
            r.f = (r.a != 0) ? 0 : 0x80;
            r.m = 1;
        }

        public void XORHL() {
            r.a ^= mmu.rb((r.h << 8) + r.l);
            r.a &= 255;
            r.f = (r.a != 0) ? 0 : 0x80;
            r.m = 2;
        }

        public void XORn() {
            r.a ^= mmu.rb(r.pc);
            r.pc++;
            r.a &= 255;
            r.f = (r.a != 0) ? 0 : 0x80;
            r.m = 2;
        }

        public void INCr_b() {
            r.b++;
            r.b &= 255;
            r.f = (r.b != 0) ? 0 : 0x80;
            r.m = 1;
        }

        public void INCr_c() {
            r.c++;
            r.c &= 255;
            r.f = (r.c != 0) ? 0 : 0x80;
            r.m = 1;
        }

        public void INCr_d() {
            r.d++;
            r.d &= 255;
            r.f = (r.d != 0) ? 0 : 0x80;
            r.m = 1;
        }

        public void INCr_e() {
            r.e++;
            r.e &= 255;
            r.f = (r.e != 0) ? 0 : 0x80;
            r.m = 1;
        }

        public void INCr_h() {
            r.h++;
            r.h &= 255;
            r.f = (r.h != 0) ? 0 : 0x80;
            r.m = 1;
        }

        public void INCr_l() {
            r.l++;
            r.l &= 255;
            r.f = (r.l != 0) ? 0 : 0x80;
            r.m = 1;
        }

        public void INCr_a() {
            r.a++;
            r.a &= 255;
            r.f = (r.a != 0) ? 0 : 0x80;
            r.m = 1;
        }

        public void INCHLm() {
            int i = mmu.rb((r.h << 8) + r.l) + 1;
            i &= 255;
            mmu.wb((r.h << 8) + r.l, i);
            r.f = (i != 0) ? 0 : 0x80;
            r.m = 3;
        }

        public void DECr_b() {
            r.b--;
            r.b &= 255;
            r.f = (r.b != 0) ? 0 : 0x80;
            r.m = 1;
        }

        public void DECr_c() {
            r.c--;
            r.c &= 255;
            r.f = (r.c != 0) ? 0 : 0x80;
            r.m = 1;
        }

        public void DECr_d() {
            r.d--;
            r.d &= 255;
            r.f = (r.d != 0) ? 0 : 0x80;
            r.m = 1;
        }

        public void DECr_e() {
            r.e--;
            r.e &= 255;
            r.f = (r.e != 0) ? 0 : 0x80;
            r.m = 1;
        }

        public void DECr_h() {
            r.h--;
            r.h &= 255;
            r.f = (r.h != 0) ? 0 : 0x80;
            r.m = 1;
        }

        public void DECr_l() {
            r.l--;
            r.l &= 255;
            r.f = (r.l != 0) ? 0 : 0x80;
            r.m = 1;
        }

        public void DECr_a() {
            r.a--;
            r.a &= 255;
            r.f = (r.a != 0) ? 0 : 0x80;
            r.m = 1;
        }

        public void DECHLm() {
            int i = mmu.rb((r.h << 8) + r.l) - 1;
            i &= 255;
            mmu.wb((r.h << 8) + r.l, i);
            r.f = (i != 0) ? 0 : 0x80;
            r.m = 3;
        }

        public void INCBC() {
            r.c = (r.c + 1) & 255;
            if (r.c != 0)
                r.b = (r.b + 1) & 255;
            r.m = 1;
        }

        public void INCDE() {
            r.e = (r.e + 1) & 255;
            if (r.e != 0)
                r.d = (r.d + 1) & 255;
            r.m = 1;
        }

        public void INCHL() {
            r.l = (r.l + 1) & 255;
            if (r.l != 0)
                r.h = (r.h + 1) & 255;
            r.m = 1;
        }

        public void INCSP() {
            r.sp = (r.sp + 1) & 65535;
            r.m = 1;
        }

        public void DECBC() {
            r.c = (r.c - 1) & 255;
            if (r.c == 255)
                r.b = (r.b - 1) & 255;
            r.m = 1;
        }

        public void DECDE() {
            r.e = (r.e - 1) & 255;
            if (r.e == 255)
                r.d = (r.d - 1) & 255;
            r.m = 1;
        }

        public void DECHL() {
            r.l = (r.l - 1) & 255;
            if (r.l == 255)
                r.h = (r.h - 1) & 255;
            r.m = 1;
        }

        public void DECSP() {
            r.sp = (r.sp - 1) & 65535;
            r.m = 1;
        }

        /*--- Bit manipulation ---*/
        public void BIT0b() {
            r.f &= 0x1F;
            r.f |= 0x20;
            r.f = ((r.b & 0x01) != 0) ? 0 : 0x80;
            r.m = 2;
        }

        public void BIT0c() {
            r.f &= 0x1F;
            r.f |= 0x20;
            r.f = ((r.c & 0x01) != 0) ? 0 : 0x80;
            r.m = 2;
        }

        public void BIT0d() {
            r.f &= 0x1F;
            r.f |= 0x20;
            r.f = ((r.d & 0x01) != 0) ? 0 : 0x80;
            r.m = 2;
        }

        public void BIT0e() {
            r.f &= 0x1F;
            r.f |= 0x20;
            r.f = ((r.e & 0x01) != 0) ? 0 : 0x80;
            r.m = 2;
        }

        public void BIT0h() {
            r.f &= 0x1F;
            r.f |= 0x20;
            r.f = ((r.h & 0x01) != 0) ? 0 : 0x80;
            r.m = 2;
        }

        public void BIT0l() {
            r.f &= 0x1F;
            r.f |= 0x20;
            r.f = ((r.l & 0x01) != 0) ? 0 : 0x80;
            r.m = 2;
        }

        public void BIT0a() {
            r.f &= 0x1F;
            r.f |= 0x20;
            r.f = ((r.a & 0x01) != 0) ? 0 : 0x80;
            r.m = 2;
        }

        public void BIT0m() {
            r.f &= 0x1F;
            r.f |= 0x20;
            r.f = ((mmu.rb((r.h << 8) + r.l) & 0x01) != 0) ? 0 : 0x80;
            r.m = 3;
        }

        public void RES0b() {
            r.b &= 0xFE;
            r.m = 2;
        }

        public void RES0c() {
            r.c &= 0xFE;
            r.m = 2;
        }

        public void RES0d() {
            r.d &= 0xFE;
            r.m = 2;
        }

        public void RES0e() {
            r.e &= 0xFE;
            r.m = 2;
        }

        public void RES0h() {
            r.h &= 0xFE;
            r.m = 2;
        }

        public void RES0l() {
            r.l &= 0xFE;
            r.m = 2;
        }

        public void RES0a() {
            r.a &= 0xFE;
            r.m = 2;
        }

        public void RES0m() {
            int i = mmu.rb((r.h << 8) + r.l);
            i &= 0xFE;
            mmu.wb((r.h << 8) + r.l, i);
            r.m = 4;
        }

        public void SET0b() {
            r.b |= 0x01;
            r.m = 2;
        }

        public void SET0c() {
            r.b |= 0x01;
            r.m = 2;
        }

        public void SET0d() {
            r.b |= 0x01;
            r.m = 2;
        }

        public void SET0e() {
            r.b |= 0x01;
            r.m = 2;
        }

        public void SET0h() {
            r.b |= 0x01;
            r.m = 2;
        }

        public void SET0l() {
            r.b |= 0x01;
            r.m = 2;
        }

        public void SET0a() {
            r.b |= 0x01;
            r.m = 2;
        }

        public void SET0m() {
            int i = mmu.rb((r.h << 8) + r.l);
            i |= 0x01;
            mmu.wb((r.h << 8) + r.l, i);
            r.m = 4;
        }

        public void BIT1b() {
            r.f &= 0x1F;
            r.f |= 0x20;
            r.f = ((r.b & 0x02) != 0) ? 0 : 0x80;
            r.m = 2;
        }

        public void BIT1c() {
            r.f &= 0x1F;
            r.f |= 0x20;
            r.f = ((r.c & 0x02) != 0) ? 0 : 0x80;
            r.m = 2;
        }

        public void BIT1d() {
            r.f &= 0x1F;
            r.f |= 0x20;
            r.f = ((r.d & 0x02) != 0) ? 0 : 0x80;
            r.m = 2;
        }

        public void BIT1e() {
            r.f &= 0x1F;
            r.f |= 0x20;
            r.f = ((r.e & 0x02) != 0) ? 0 : 0x80;
            r.m = 2;
        }

        public void BIT1h() {
            r.f &= 0x1F;
            r.f |= 0x20;
            r.f = ((r.h & 0x02) != 0) ? 0 : 0x80;
            r.m = 2;
        }

        public void BIT1l() {
            r.f &= 0x1F;
            r.f |= 0x20;
            r.f = ((r.l & 0x02) != 0) ? 0 : 0x80;
            r.m = 2;
        }

        public void BIT1a() {
            r.f &= 0x1F;
            r.f |= 0x20;
            r.f = ((r.a & 0x02) != 0) ? 0 : 0x80;
            r.m = 2;
        }

        public void BIT1m() {
            r.f &= 0x1F;
            r.f |= 0x20;
            r.f = ((mmu.rb((r.h << 8) + r.l) & 0x02) != 0) ? 0 : 0x80;
            r.m = 3;
        }

        public void RES1b() {
            r.b &= 0xFD;
            r.m = 2;
        }

        public void RES1c() {
            r.c &= 0xFD;
            r.m = 2;
        }

        public void RES1d() {
            r.d &= 0xFD;
            r.m = 2;
        }

        public void RES1e() {
            r.e &= 0xFD;
            r.m = 2;
        }

        public void RES1h() {
            r.h &= 0xFD;
            r.m = 2;
        }

        public void RES1l() {
            r.l &= 0xFD;
            r.m = 2;
        }

        public void RES1a() {
            r.a &= 0xFD;
            r.m = 2;
        }

        public void RES1m() {
            int i = mmu.rb((r.h << 8) + r.l);
            i &= 0xFD;
            mmu.wb((r.h << 8) + r.l, i);
            r.m = 4;
        }

        public void SET1b() {
            r.b |= 0x02;
            r.m = 2;
        }

        public void SET1c() {
            r.b |= 0x02;
            r.m = 2;
        }

        public void SET1d() {
            r.b |= 0x02;
            r.m = 2;
        }

        public void SET1e() {
            r.b |= 0x02;
            r.m = 2;
        }

        public void SET1h() {
            r.b |= 0x02;
            r.m = 2;
        }

        public void SET1l() {
            r.b |= 0x02;
            r.m = 2;
        }

        public void SET1a() {
            r.b |= 0x02;
            r.m = 2;
        }

        public void SET1m() {
            int i = mmu.rb((r.h << 8) + r.l);
            i |= 0x02;
            mmu.wb((r.h << 8) + r.l, i);
            r.m = 4;
        }

        public void BIT2b() {
            r.f &= 0x1F;
            r.f |= 0x20;
            r.f = ((r.b & 0x04) != 0) ? 0 : 0x80;
            r.m = 2;
        }

        public void BIT2c() {
            r.f &= 0x1F;
            r.f |= 0x20;
            r.f = ((r.c & 0x04) != 0) ? 0 : 0x80;
            r.m = 2;
        }

        public void BIT2d() {
            r.f &= 0x1F;
            r.f |= 0x20;
            r.f = ((r.d & 0x04) != 0) ? 0 : 0x80;
            r.m = 2;
        }

        public void BIT2e() {
            r.f &= 0x1F;
            r.f |= 0x20;
            r.f = ((r.e & 0x04) != 0) ? 0 : 0x80;
            r.m = 2;
        }

        public void BIT2h() {
            r.f &= 0x1F;
            r.f |= 0x20;
            r.f = ((r.h & 0x04) != 0) ? 0 : 0x80;
            r.m = 2;
        }

        public void BIT2l() {
            r.f &= 0x1F;
            r.f |= 0x20;
            r.f = ((r.l & 0x04) != 0) ? 0 : 0x80;
            r.m = 2;
        }

        public void BIT2a() {
            r.f &= 0x1F;
            r.f |= 0x20;
            r.f = ((r.a & 0x04) != 0) ? 0 : 0x80;
            r.m = 2;
        }

        public void BIT2m() {
            r.f &= 0x1F;
            r.f |= 0x20;
            r.f = ((mmu.rb((r.h << 8) + r.l) & 0x04) != 0) ? 0 : 0x80;
            r.m = 3;
        }

        public void RES2b() {
            r.b &= 0xFB;
            r.m = 2;
        }

        public void RES2c() {
            r.c &= 0xFB;
            r.m = 2;
        }

        public void RES2d() {
            r.d &= 0xFB;
            r.m = 2;
        }

        public void RES2e() {
            r.e &= 0xFB;
            r.m = 2;
        }

        public void RES2h() {
            r.h &= 0xFB;
            r.m = 2;
        }

        public void RES2l() {
            r.l &= 0xFB;
            r.m = 2;
        }

        public void RES2a() {
            r.a &= 0xFB;
            r.m = 2;
        }

        public void RES2m() {
            int i = mmu.rb((r.h << 8) + r.l);
            i &= 0xFB;
            mmu.wb((r.h << 8) + r.l, i);
            r.m = 4;
        }

        public void SET2b() {
            r.b |= 0x04;
            r.m = 2;
        }

        public void SET2c() {
            r.b |= 0x04;
            r.m = 2;
        }

        public void SET2d() {
            r.b |= 0x04;
            r.m = 2;
        }

        public void SET2e() {
            r.b |= 0x04;
            r.m = 2;
        }

        public void SET2h() {
            r.b |= 0x04;
            r.m = 2;
        }

        public void SET2l() {
            r.b |= 0x04;
            r.m = 2;
        }

        public void SET2a() {
            r.b |= 0x04;
            r.m = 2;
        }

        public void SET2m() {
            int i = mmu.rb((r.h << 8) + r.l);
            i |= 0x04;
            mmu.wb((r.h << 8) + r.l, i);
            r.m = 4;
        }

        public void BIT3b() {
            r.f &= 0x1F;
            r.f |= 0x20;
            r.f = ((r.b & 0x08) != 0) ? 0 : 0x80;
            r.m = 2;
        }

        public void BIT3c() {
            r.f &= 0x1F;
            r.f |= 0x20;
            r.f = ((r.c & 0x08) != 0) ? 0 : 0x80;
            r.m = 2;
        }

        public void BIT3d() {
            r.f &= 0x1F;
            r.f |= 0x20;
            r.f = ((r.d & 0x08) != 0) ? 0 : 0x80;
            r.m = 2;
        }

        public void BIT3e() {
            r.f &= 0x1F;
            r.f |= 0x20;
            r.f = ((r.e & 0x08) != 0) ? 0 : 0x80;
            r.m = 2;
        }

        public void BIT3h() {
            r.f &= 0x1F;
            r.f |= 0x20;
            r.f = ((r.h & 0x08) != 0) ? 0 : 0x80;
            r.m = 2;
        }

        public void BIT3l() {
            r.f &= 0x1F;
            r.f |= 0x20;
            r.f = ((r.l & 0x08) != 0) ? 0 : 0x80;
            r.m = 2;
        }

        public void BIT3a() {
            r.f &= 0x1F;
            r.f |= 0x20;
            r.f = ((r.a & 0x08) != 0) ? 0 : 0x80;
            r.m = 2;
        }

        public void BIT3m() {
            r.f &= 0x1F;
            r.f |= 0x20;
            r.f = ((mmu.rb((r.h << 8) + r.l) & 0x08) != 0) ? 0 : 0x80;
            r.m = 3;
        }

        public void RES3b() {
            r.b &= 0xF7;
            r.m = 2;
        }

        public void RES3c() {
            r.c &= 0xF7;
            r.m = 2;
        }

        public void RES3d() {
            r.d &= 0xF7;
            r.m = 2;
        }

        public void RES3e() {
            r.e &= 0xF7;
            r.m = 2;
        }

        public void RES3h() {
            r.h &= 0xF7;
            r.m = 2;
        }

        public void RES3l() {
            r.l &= 0xF7;
            r.m = 2;
        }

        public void RES3a() {
            r.a &= 0xF7;
            r.m = 2;
        }

        public void RES3m() {
            int i = mmu.rb((r.h << 8) + r.l);
            i &= 0xF7;
            mmu.wb((r.h << 8) + r.l, i);
            r.m = 4;
        }

        public void SET3b() {
            r.b |= 0x08;
            r.m = 2;
        }

        public void SET3c() {
            r.b |= 0x08;
            r.m = 2;
        }

        public void SET3d() {
            r.b |= 0x08;
            r.m = 2;
        }

        public void SET3e() {
            r.b |= 0x08;
            r.m = 2;
        }

        public void SET3h() {
            r.b |= 0x08;
            r.m = 2;
        }

        public void SET3l() {
            r.b |= 0x08;
            r.m = 2;
        }

        public void SET3a() {
            r.b |= 0x08;
            r.m = 2;
        }

        public void SET3m() {
            int i = mmu.rb((r.h << 8) + r.l);
            i |= 0x08;
            mmu.wb((r.h << 8) + r.l, i);
            r.m = 4;
        }

        public void BIT4b() {
            r.f &= 0x1F;
            r.f |= 0x20;
            r.f = ((r.b & 0x10) != 0) ? 0 : 0x80;
            r.m = 2;
        }

        public void BIT4c() {
            r.f &= 0x1F;
            r.f |= 0x20;
            r.f = ((r.c & 0x10) != 0) ? 0 : 0x80;
            r.m = 2;
        }

        public void BIT4d() {
            r.f &= 0x1F;
            r.f |= 0x20;
            r.f = ((r.d & 0x10) != 0) ? 0 : 0x80;
            r.m = 2;
        }

        public void BIT4e() {
            r.f &= 0x1F;
            r.f |= 0x20;
            r.f = ((r.e & 0x10) != 0) ? 0 : 0x80;
            r.m = 2;
        }

        public void BIT4h() {
            r.f &= 0x1F;
            r.f |= 0x20;
            r.f = ((r.h & 0x10) != 0) ? 0 : 0x80;
            r.m = 2;
        }

        public void BIT4l() {
            r.f &= 0x1F;
            r.f |= 0x20;
            r.f = ((r.l & 0x10) != 0) ? 0 : 0x80;
            r.m = 2;
        }

        public void BIT4a() {
            r.f &= 0x1F;
            r.f |= 0x20;
            r.f = ((r.a & 0x10) != 0) ? 0 : 0x80;
            r.m = 2;
        }

        public void BIT4m() {
            r.f &= 0x1F;
            r.f |= 0x20;
            r.f = ((mmu.rb((r.h << 8) + r.l) & 0x10) != 0) ? 0 : 0x80;
            r.m = 3;
        }

        public void RES4b() {
            r.b &= 0xEF;
            r.m = 2;
        }

        public void RES4c() {
            r.c &= 0xEF;
            r.m = 2;
        }

        public void RES4d() {
            r.d &= 0xEF;
            r.m = 2;
        }

        public void RES4e() {
            r.e &= 0xEF;
            r.m = 2;
        }

        public void RES4h() {
            r.h &= 0xEF;
            r.m = 2;
        }

        public void RES4l() {
            r.l &= 0xEF;
            r.m = 2;
        }

        public void RES4a() {
            r.a &= 0xEF;
            r.m = 2;
        }

        public void RES4m() {
            int i = mmu.rb((r.h << 8) + r.l);
            i &= 0xEF;
            mmu.wb((r.h << 8) + r.l, i);
            r.m = 4;
        }

        public void SET4b() {
            r.b |= 0x10;
            r.m = 2;
        }

        public void SET4c() {
            r.b |= 0x10;
            r.m = 2;
        }

        public void SET4d() {
            r.b |= 0x10;
            r.m = 2;
        }

        public void SET4e() {
            r.b |= 0x10;
            r.m = 2;
        }

        public void SET4h() {
            r.b |= 0x10;
            r.m = 2;
        }

        public void SET4l() {
            r.b |= 0x10;
            r.m = 2;
        }

        public void SET4a() {
            r.b |= 0x10;
            r.m = 2;
        }

        public void SET4m() {
            int i = mmu.rb((r.h << 8) + r.l);
            i |= 0x10;
            mmu.wb((r.h << 8) + r.l, i);
            r.m = 4;
        }

        public void BIT5b() {
            r.f &= 0x1F;
            r.f |= 0x20;
            r.f = ((r.b & 0x20) != 0) ? 0 : 0x80;
            r.m = 2;
        }

        public void BIT5c() {
            r.f &= 0x1F;
            r.f |= 0x20;
            r.f = ((r.c & 0x20) != 0) ? 0 : 0x80;
            r.m = 2;
        }

        public void BIT5d() {
            r.f &= 0x1F;
            r.f |= 0x20;
            r.f = ((r.d & 0x20) != 0) ? 0 : 0x80;
            r.m = 2;
        }

        public void BIT5e() {
            r.f &= 0x1F;
            r.f |= 0x20;
            r.f = ((r.e & 0x20) != 0) ? 0 : 0x80;
            r.m = 2;
        }

        public void BIT5h() {
            r.f &= 0x1F;
            r.f |= 0x20;
            r.f = ((r.h & 0x20) != 0) ? 0 : 0x80;
            r.m = 2;
        }

        public void BIT5l() {
            r.f &= 0x1F;
            r.f |= 0x20;
            r.f = ((r.l & 0x20) != 0) ? 0 : 0x80;
            r.m = 2;
        }

        public void BIT5a() {
            r.f &= 0x1F;
            r.f |= 0x20;
            r.f = ((r.a & 0x20) != 0) ? 0 : 0x80;
            r.m = 2;
        }

        public void BIT5m() {
            r.f &= 0x1F;
            r.f |= 0x20;
            r.f = ((mmu.rb((r.h << 8) + r.l) & 0x20) != 0) ? 0 : 0x80;
            r.m = 3;
        }

        public void RES5b() {
            r.b &= 0xDF;
            r.m = 2;
        }

        public void RES5c() {
            r.c &= 0xDF;
            r.m = 2;
        }

        public void RES5d() {
            r.d &= 0xDF;
            r.m = 2;
        }

        public void RES5e() {
            r.e &= 0xDF;
            r.m = 2;
        }

        public void RES5h() {
            r.h &= 0xDF;
            r.m = 2;
        }

        public void RES5l() {
            r.l &= 0xDF;
            r.m = 2;
        }

        public void RES5a() {
            r.a &= 0xDF;
            r.m = 2;
        }

        public void RES5m() {
            int i = mmu.rb((r.h << 8) + r.l);
            i &= 0xDF;
            mmu.wb((r.h << 8) + r.l, i);
            r.m = 4;
        }

        public void SET5b() {
            r.b |= 0x20;
            r.m = 2;
        }

        public void SET5c() {
            r.b |= 0x20;
            r.m = 2;
        }

        public void SET5d() {
            r.b |= 0x20;
            r.m = 2;
        }

        public void SET5e() {
            r.b |= 0x20;
            r.m = 2;
        }

        public void SET5h() {
            r.b |= 0x20;
            r.m = 2;
        }

        public void SET5l() {
            r.b |= 0x20;
            r.m = 2;
        }

        public void SET5a() {
            r.b |= 0x20;
            r.m = 2;
        }

        public void SET5m() {
            int i = mmu.rb((r.h << 8) + r.l);
            i |= 0x20;
            mmu.wb((r.h << 8) + r.l, i);
            r.m = 4;
        }

        public void BIT6b() {
            r.f &= 0x1F;
            r.f |= 0x20;
            r.f = ((r.b & 0x40) != 0) ? 0 : 0x80;
            r.m = 2;
        }

        public void BIT6c() {
            r.f &= 0x1F;
            r.f |= 0x20;
            r.f = ((r.c & 0x40) != 0) ? 0 : 0x80;
            r.m = 2;
        }

        public void BIT6d() {
            r.f &= 0x1F;
            r.f |= 0x20;
            r.f = ((r.d & 0x40) != 0) ? 0 : 0x80;
            r.m = 2;
        }

        public void BIT6e() {
            r.f &= 0x1F;
            r.f |= 0x20;
            r.f = ((r.e & 0x40) != 0) ? 0 : 0x80;
            r.m = 2;
        }

        public void BIT6h() {
            r.f &= 0x1F;
            r.f |= 0x20;
            r.f = ((r.h & 0x40) != 0) ? 0 : 0x80;
            r.m = 2;
        }

        public void BIT6l() {
            r.f &= 0x1F;
            r.f |= 0x20;
            r.f = ((r.l & 0x40) != 0) ? 0 : 0x80;
            r.m = 2;
        }

        public void BIT6a() {
            r.f &= 0x1F;
            r.f |= 0x20;
            r.f = ((r.a & 0x40) != 0) ? 0 : 0x80;
            r.m = 2;
        }

        public void BIT6m() {
            r.f &= 0x1F;
            r.f |= 0x20;
            r.f = ((mmu.rb((r.h << 8) + r.l) & 0x40) != 0) ? 0 : 0x80;
            r.m = 3;
        }

        public void RES6b() {
            r.b &= 0xBF;
            r.m = 2;
        }

        public void RES6c() {
            r.c &= 0xBF;
            r.m = 2;
        }

        public void RES6d() {
            r.d &= 0xBF;
            r.m = 2;
        }

        public void RES6e() {
            r.e &= 0xBF;
            r.m = 2;
        }

        public void RES6h() {
            r.h &= 0xBF;
            r.m = 2;
        }

        public void RES6l() {
            r.l &= 0xBF;
            r.m = 2;
        }

        public void RES6a() {
            r.a &= 0xBF;
            r.m = 2;
        }

        public void RES6m() {
            int i = mmu.rb((r.h << 8) + r.l);
            i &= 0xBF;
            mmu.wb((r.h << 8) + r.l, i);
            r.m = 4;
        }

        public void SET6b() {
            r.b |= 0x40;
            r.m = 2;
        }

        public void SET6c() {
            r.b |= 0x40;
            r.m = 2;
        }

        public void SET6d() {
            r.b |= 0x40;
            r.m = 2;
        }

        public void SET6e() {
            r.b |= 0x40;
            r.m = 2;
        }

        public void SET6h() {
            r.b |= 0x40;
            r.m = 2;
        }

        public void SET6l() {
            r.b |= 0x40;
            r.m = 2;
        }

        public void SET6a() {
            r.b |= 0x40;
            r.m = 2;
        }

        public void SET6m() {
            int i = mmu.rb((r.h << 8) + r.l);
            i |= 0x40;
            mmu.wb((r.h << 8) + r.l, i);
            r.m = 4;
        }

        public void BIT7b() {
            r.f &= 0x1F;
            r.f |= 0x20;
            r.f = ((r.b & 0x80) != 0) ? 0 : 0x80;
            r.m = 2;
        }

        public void BIT7c() {
            r.f &= 0x1F;
            r.f |= 0x20;
            r.f = ((r.c & 0x80) != 0) ? 0 : 0x80;
            r.m = 2;
        }

        public void BIT7d() {
            r.f &= 0x1F;
            r.f |= 0x20;
            r.f = ((r.d & 0x80) != 0) ? 0 : 0x80;
            r.m = 2;
        }

        public void BIT7e() {
            r.f &= 0x1F;
            r.f |= 0x20;
            r.f = ((r.e & 0x80) != 0) ? 0 : 0x80;
            r.m = 2;
        }

        public void BIT7h() {
            r.f &= 0x1F;
            r.f |= 0x20;
            r.f = ((r.h & 0x80) != 0) ? 0 : 0x80;
            r.m = 2;
        }

        public void BIT7l() {
            r.f &= 0x1F;
            r.f |= 0x20;
            r.f = ((r.l & 0x80) != 0) ? 0 : 0x80;
            r.m = 2;
        }

        public void BIT7a() {
            r.f &= 0x1F;
            r.f |= 0x20;
            r.f = ((r.a & 0x80) != 0) ? 0 : 0x80;
            r.m = 2;
        }

        public void BIT7m() {
            r.f &= 0x1F;
            r.f |= 0x20;
            r.f = ((mmu.rb((r.h << 8) + r.l) & 0x80) != 0) ? 0 : 0x80;
            r.m = 3;
        }

        public void RES7b() {
            r.b &= 0x7F;
            r.m = 2;
        }

        public void RES7c() {
            r.c &= 0x7F;
            r.m = 2;
        }

        public void RES7d() {
            r.d &= 0x7F;
            r.m = 2;
        }

        public void RES7e() {
            r.e &= 0x7F;
            r.m = 2;
        }

        public void RES7h() {
            r.h &= 0x7F;
            r.m = 2;
        }

        public void RES7l() {
            r.l &= 0x7F;
            r.m = 2;
        }

        public void RES7a() {
            r.a &= 0x7F;
            r.m = 2;
        }

        public void RES7m() {
            int i = mmu.rb((r.h << 8) + r.l);
            i &= 0x7F;
            mmu.wb((r.h << 8) + r.l, i);
            r.m = 4;
        }

        public void SET7b() {
            r.b |= 0x80;
            r.m = 2;
        }

        public void SET7c() {
            r.b |= 0x80;
            r.m = 2;
        }

        public void SET7d() {
            r.b |= 0x80;
            r.m = 2;
        }

        public void SET7e() {
            r.b |= 0x80;
            r.m = 2;
        }

        public void SET7h() {
            r.b |= 0x80;
            r.m = 2;
        }

        public void SET7l() {
            r.b |= 0x80;
            r.m = 2;
        }

        public void SET7a() {
            r.b |= 0x80;
            r.m = 2;
        }

        public void SET7m() {
            int i = mmu.rb((r.h << 8) + r.l);
            i |= 0x80;
            mmu.wb((r.h << 8) + r.l, i);
            r.m = 4;
        }

        public void RLA() {
            int ci = ((r.f & 0x10) != 0) ? 1 : 0;
            int co = ((r.a & 0x80) != 0) ? 0x10 : 0;
            r.a = (r.a << 1) + ci;
            r.a &= 255;
            r.f = (r.f & 0xEF) + co;
            r.m = 1;
        }

        public void RLCA() {
            int ci = ((r.a & 0x80) != 0) ? 1 : 0;
            int co = ((r.a & 0x80) != 0) ? 0x10 : 0;
            r.a = (r.a << 1) + ci;
            r.a &= 255;
            r.f = (r.f & 0xEF) + co;
            r.m = 1;
        }

        public void RRA() {
            int ci = ((r.f & 0x10) != 0) ? 0x80 : 0;
            int co = ((r.a & 1) != 0) ? 0x10 : 0;
            r.a = (r.a >> 1) + ci;
            r.a &= 255;
            r.f = (r.f & 0xEF) + co;
            r.m = 1;
        }

        public void RRCA() {
            int ci = ((r.a & 1) != 0) ? 0x80 : 0;
            int co = ((r.a & 1) != 0) ? 0x10 : 0;
            r.a = (r.a >> 1) + ci;
            r.a &= 255;
            r.f = (r.f & 0xEF) + co;
            r.m = 1;
        }

        public void RLr_b() {
            int ci = ((r.f & 0x10) != 0) ? 1 : 0;
            int co = ((r.b & 0x80) != 0) ? 0x10 : 0;
            r.b = (r.b << 1) + ci;
            r.b &= 255;
            r.f = ((r.b) != 0) ? 0 : 0x80;
            r.f = (r.f & 0xEF) + co;
            r.m = 2;
        }

        public void RLr_c() {
            int ci = ((r.f & 0x10) != 0) ? 1 : 0;
            int co = ((r.c & 0x80) != 0) ? 0x10 : 0;
            r.c = (r.c << 1) + ci;
            r.c &= 255;
            r.f = ((r.c) != 0) ? 0 : 0x80;
            r.f = (r.f & 0xEF) + co;
            r.m = 2;
        }

        public void RLr_d() {
            int ci = ((r.f & 0x10) != 0) ? 1 : 0;
            int co = ((r.d & 0x80) != 0) ? 0x10 : 0;
            r.d = (r.d << 1) + ci;
            r.d &= 255;
            r.f = ((r.d) != 0) ? 0 : 0x80;
            r.f = (r.f & 0xEF) + co;
            r.m = 2;
        }

        public void RLr_e() {
            int ci = ((r.f & 0x10) != 0) ? 1 : 0;
            int co = ((r.e & 0x80) != 0) ? 0x10 : 0;
            r.e = (r.e << 1) + ci;
            r.e &= 255;
            r.f = ((r.e) != 0) ? 0 : 0x80;
            r.f = (r.f & 0xEF) + co;
            r.m = 2;
        }

        public void RLr_h() {
            int ci = ((r.f & 0x10) != 0) ? 1 : 0;
            int co = ((r.h & 0x80) != 0) ? 0x10 : 0;
            r.h = (r.h << 1) + ci;
            r.h &= 255;
            r.f = ((r.h) != 0) ? 0 : 0x80;
            r.f = (r.f & 0xEF) + co;
            r.m = 2;
        }

        public void RLr_l() {
            int ci = ((r.f & 0x10) != 0) ? 1 : 0;
            int co = ((r.l & 0x80) != 0) ? 0x10 : 0;
            r.l = (r.l << 1) + ci;
            r.l &= 255;
            r.f = ((r.l) != 0) ? 0 : 0x80;
            r.f = (r.f & 0xEF) + co;
            r.m = 2;
        }

        public void RLr_a() {
            int ci = ((r.f & 0x10) != 0) ? 1 : 0;
            int co = ((r.a & 0x80) != 0) ? 0x10 : 0;
            r.a = (r.a << 1) + ci;
            r.a &= 255;
            r.f = ((r.a) != 0) ? 0 : 0x80;
            r.f = (r.f & 0xEF) + co;
            r.m = 2;
        }

        public void RLHL() {
            int i = mmu.rb((r.h << 8) + r.l);
            int ci = ((r.f & 0x10) != 0) ? 1 : 0;
            int co = ((i & 0x80) != 0) ? 0x10 : 0;
            i = (i << 1) + ci;
            i &= 255;
            r.f = ((i) != 0) ? 0 : 0x80;
            mmu.wb((r.h << 8) + r.l, i);
            r.f = (r.f & 0xEF) + co;
            r.m = 4;
        }

        public void RLCr_b() {
            int ci = ((r.b & 0x80) != 0) ? 1 : 0;
            int co = ((r.b & 0x80) != 0) ? 0x10 : 0;
            r.b = (r.b << 1) + ci;
            r.b &= 255;
            r.f = ((r.b) != 0) ? 0 : 0x80;
            r.f = (r.f & 0xEF) + co;
            r.m = 2;
        }

        public void RLCr_c() {
            int ci = ((r.c & 0x80) != 0) ? 1 : 0;
            int co = ((r.c & 0x80) != 0) ? 0x10 : 0;
            r.c = (r.c << 1) + ci;
            r.c &= 255;
            r.f = ((r.c) != 0) ? 0 : 0x80;
            r.f = (r.f & 0xEF) + co;
            r.m = 2;
        }

        public void RLCr_d() {
            int ci = ((r.d & 0x80) != 0) ? 1 : 0;
            int co = ((r.d & 0x80) != 0) ? 0x10 : 0;
            r.d = (r.d << 1) + ci;
            r.d &= 255;
            r.f = ((r.d) != 0) ? 0 : 0x80;
            r.f = (r.f & 0xEF) + co;
            r.m = 2;
        }

        public void RLCr_e() {
            int ci = ((r.e & 0x80) != 0) ? 1 : 0;
            int co = ((r.e & 0x80) != 0) ? 0x10 : 0;
            r.e = (r.e << 1) + ci;
            r.e &= 255;
            r.f = ((r.e) != 0) ? 0 : 0x80;
            r.f = (r.f & 0xEF) + co;
            r.m = 2;
        }

        public void RLCr_h() {
            int ci = ((r.h & 0x80) != 0) ? 1 : 0;
            int co = ((r.h & 0x80) != 0) ? 0x10 : 0;
            r.h = (r.h << 1) + ci;
            r.h &= 255;
            r.f = ((r.h) != 0) ? 0 : 0x80;
            r.f = (r.f & 0xEF) + co;
            r.m = 2;
        }

        public void RLCr_l() {
            int ci = ((r.l & 0x80) != 0) ? 1 : 0;
            int co = ((r.l & 0x80) != 0) ? 0x10 : 0;
            r.l = (r.l << 1) + ci;
            r.l &= 255;
            r.f = ((r.l) != 0) ? 0 : 0x80;
            r.f = (r.f & 0xEF) + co;
            r.m = 2;
        }

        public void RLCr_a() {
            int ci = ((r.a & 0x80) != 0) ? 1 : 0;
            int co = ((r.a & 0x80) != 0) ? 0x10 : 0;
            r.a = (r.a << 1) + ci;
            r.a &= 255;
            r.f = ((r.a) != 0) ? 0 : 0x80;
            r.f = (r.f & 0xEF) + co;
            r.m = 2;
        }

        public void RLCHL() {
            int i = mmu.rb((r.h << 8) + r.l);
            int ci = ((i & 0x80) != 0) ? 1 : 0;
            int co = ((i & 0x80) != 0) ? 0x10 : 0;
            i = (i << 1) + ci;
            i &= 255;
            r.f = ((i) != 0) ? 0 : 0x80;
            mmu.wb((r.h << 8) + r.l, i);
            r.f = (r.f & 0xEF) + co;
            r.m = 4;
        }

        public void RRr_b() {
            int ci = ((r.f & 0x10) != 0) ? 0x80 : 0;
            int co = ((r.b & 1) != 0) ? 0x10 : 0;
            r.b = (r.b >> 1) + ci;
            r.b &= 255;
            r.f = ((r.b) != 0) ? 0 : 0x80;
            r.f = (r.f & 0xEF) + co;
            r.m = 2;
        }

        public void RRr_c() {
            int ci = ((r.f & 0x10) != 0) ? 0x80 : 0;
            int co = ((r.c & 1) != 0) ? 0x10 : 0;
            r.c = (r.c >> 1) + ci;
            r.c &= 255;
            r.f = ((r.c) != 0) ? 0 : 0x80;
            r.f = (r.f & 0xEF) + co;
            r.m = 2;
        }

        public void RRr_d() {
            int ci = ((r.f & 0x10) != 0) ? 0x80 : 0;
            int co = ((r.d & 1) != 0) ? 0x10 : 0;
            r.d = (r.d >> 1) + ci;
            r.d &= 255;
            r.f = ((r.d) != 0) ? 0 : 0x80;
            r.f = (r.f & 0xEF) + co;
            r.m = 2;
        }

        public void RRr_e() {
            int ci = ((r.f & 0x10) != 0) ? 0x80 : 0;
            int co = ((r.e & 1) != 0) ? 0x10 : 0;
            r.e = (r.e >> 1) + ci;
            r.e &= 255;
            r.f = ((r.e) != 0) ? 0 : 0x80;
            r.f = (r.f & 0xEF) + co;
            r.m = 2;
        }

        public void RRr_h() {
            int ci = ((r.f & 0x10) != 0) ? 0x80 : 0;
            int co = ((r.h & 1) != 0) ? 0x10 : 0;
            r.h = (r.h >> 1) + ci;
            r.h &= 255;
            r.f = ((r.h) != 0) ? 0 : 0x80;
            r.f = (r.f & 0xEF) + co;
            r.m = 2;
        }

        public void RRr_l() {
            int ci = ((r.f & 0x10) != 0) ? 0x80 : 0;
            int co = ((r.l & 1) != 0) ? 0x10 : 0;
            r.l = (r.l >> 1) + ci;
            r.l &= 255;
            r.f = ((r.l) != 0) ? 0 : 0x80;
            r.f = (r.f & 0xEF) + co;
            r.m = 2;
        }

        public void RRr_a() {
            int ci = ((r.f & 0x10) != 0) ? 0x80 : 0;
            int co = ((r.a & 1) != 0) ? 0x10 : 0;
            r.a = (r.a >> 1) + ci;
            r.a &= 255;
            r.f = ((r.a) != 0) ? 0 : 0x80;
            r.f = (r.f & 0xEF) + co;
            r.m = 2;
        }

        public void RRHL() {
            int i = mmu.rb((r.h << 8) + r.l);
            int ci = ((r.f & 0x10) != 0) ? 0x80 : 0;
            int co = ((i & 1) != 0) ? 0x10 : 0;
            i = (i >> 1) + ci;
            i &= 255;
            mmu.wb((r.h << 8) + r.l, i);
            r.f = ((i) != 0) ? 0 : 0x80;
            r.f = (r.f & 0xEF) + co;
            r.m = 4;
        }

        public void RRCr_b() {
            int ci = ((r.b & 1) != 0) ? 0x80 : 0;
            int co = ((r.b & 1) != 0) ? 0x10 : 0;
            r.b = (r.b >> 1) + ci;
            r.b &= 255;
            r.f = ((r.b) != 0) ? 0 : 0x80;
            r.f = (r.f & 0xEF) + co;
            r.m = 2;
        }

        public void RRCr_c() {
            int ci = ((r.c & 1) != 0) ? 0x80 : 0;
            int co = ((r.c & 1) != 0) ? 0x10 : 0;
            r.c = (r.c >> 1) + ci;
            r.c &= 255;
            r.f = ((r.c) != 0) ? 0 : 0x80;
            r.f = (r.f & 0xEF) + co;
            r.m = 2;
        }

        public void RRCr_d() {
            int ci = ((r.d & 1) != 0) ? 0x80 : 0;
            int co = ((r.d & 1) != 0) ? 0x10 : 0;
            r.d = (r.d >> 1) + ci;
            r.d &= 255;
            r.f = ((r.d) != 0) ? 0 : 0x80;
            r.f = (r.f & 0xEF) + co;
            r.m = 2;
        }

        public void RRCr_e() {
            int ci = ((r.e & 1) != 0) ? 0x80 : 0;
            int co = ((r.e & 1) != 0) ? 0x10 : 0;
            r.e = (r.e >> 1) + ci;
            r.e &= 255;
            r.f = ((r.e) != 0) ? 0 : 0x80;
            r.f = (r.f & 0xEF) + co;
            r.m = 2;
        }

        public void RRCr_h() {
            int ci = ((r.h & 1) != 0) ? 0x80 : 0;
            int co = ((r.h & 1) != 0) ? 0x10 : 0;
            r.h = (r.h >> 1) + ci;
            r.h &= 255;
            r.f = ((r.h) != 0) ? 0 : 0x80;
            r.f = (r.f & 0xEF) + co;
            r.m = 2;
        }

        public void RRCr_l() {
            int ci = ((r.l & 1) != 0) ? 0x80 : 0;
            int co = ((r.l & 1) != 0) ? 0x10 : 0;
            r.l = (r.l >> 1) + ci;
            r.l &= 255;
            r.f = ((r.l) != 0) ? 0 : 0x80;
            r.f = (r.f & 0xEF) + co;
            r.m = 2;
        }

        public void RRCr_a() {
            int ci = ((r.a & 1) != 0) ? 0x80 : 0;
            int co = ((r.a & 1) != 0) ? 0x10 : 0;
            r.a = (r.a >> 1) + ci;
            r.a &= 255;
            r.f = ((r.a) != 0) ? 0 : 0x80;
            r.f = (r.f & 0xEF) + co;
            r.m = 2;
        }

        public void RRCHL() {
            int i = mmu.rb((r.h << 8) + r.l);
            int ci = ((i & 1) != 0) ? 0x80 : 0;
            int co = ((i & 1) != 0) ? 0x10 : 0;
            i = (i >> 1) + ci;
            i &= 255;
            mmu.wb((r.h << 8) + r.l, i);
            r.f = ((i) != 0) ? 0 : 0x80;
            r.f = (r.f & 0xEF) + co;
            r.m = 4;
        }

        public void SLAr_b() {
            int co = ((r.b & 0x80) != 0) ? 0x10 : 0;
            r.b = (r.b << 1) & 255;
            r.f = ((r.b) != 0) ? 0 : 0x80;
            r.f = (r.f & 0xEF) + co;
            r.m = 2;
        }

        public void SLAr_c() {
            int co = ((r.c & 0x80) != 0) ? 0x10 : 0;
            r.c = (r.c << 1) & 255;
            r.f = ((r.c) != 0) ? 0 : 0x80;
            r.f = (r.f & 0xEF) + co;
            r.m = 2;
        }

        public void SLAr_d() {
            int co = ((r.d & 0x80) != 0) ? 0x10 : 0;
            r.d = (r.d << 1) & 255;
            r.f = ((r.d) != 0) ? 0 : 0x80;
            r.f = (r.f & 0xEF) + co;
            r.m = 2;
        }

        public void SLAr_e() {
            int co = ((r.e & 0x80) != 0) ? 0x10 : 0;
            r.e = (r.e << 1) & 255;
            r.f = ((r.e) != 0) ? 0 : 0x80;
            r.f = (r.f & 0xEF) + co;
            r.m = 2;
        }

        public void SLAr_h() {
            int co = ((r.h & 0x80) != 0) ? 0x10 : 0;
            r.h = (r.h << 1) & 255;
            r.f = ((r.h) != 0) ? 0 : 0x80;
            r.f = (r.f & 0xEF) + co;
            r.m = 2;
        }

        public void SLAr_l() {
            int co = ((r.l & 0x80) != 0) ? 0x10 : 0;
            r.l = (r.l << 1) & 255;
            r.f = ((r.l) != 0) ? 0 : 0x80;
            r.f = (r.f & 0xEF) + co;
            r.m = 2;
        }

        public void SLAr_a() {
            int co = ((r.a & 0x80) != 0) ? 0x10 : 0;
            r.a = (r.a << 1) & 255;
            r.f = ((r.a) != 0) ? 0 : 0x80;
            r.f = (r.f & 0xEF) + co;
            r.m = 2;
        }

        public void SLLr_b() {
            int co = ((r.b & 0x80) != 0) ? 0x10 : 0;
            r.b = (r.b << 1) & 255 + 1;
            r.f = ((r.b) != 0) ? 0 : 0x80;
            r.f = (r.f & 0xEF) + co;
            r.m = 2;
        }

        public void SLLr_c() {
            int co = ((r.c & 0x80) != 0) ? 0x10 : 0;
            r.c = (r.c << 1) & 255 + 1;
            r.f = ((r.c) != 0) ? 0 : 0x80;
            r.f = (r.f & 0xEF) + co;
            r.m = 2;
        }

        public void SLLr_d() {
            int co = ((r.d & 0x80) != 0) ? 0x10 : 0;
            r.d = (r.d << 1) & 255 + 1;
            r.f = ((r.d) != 0) ? 0 : 0x80;
            r.f = (r.f & 0xEF) + co;
            r.m = 2;
        }

        public void SLLr_e() {
            int co = ((r.e & 0x80) != 0) ? 0x10 : 0;
            r.e = (r.e << 1) & 255 + 1;
            r.f = ((r.e) != 0) ? 0 : 0x80;
            r.f = (r.f & 0xEF) + co;
            r.m = 2;
        }

        public void SLLr_h() {
            int co = ((r.h & 0x80) != 0) ? 0x10 : 0;
            r.h = (r.h << 1) & 255 + 1;
            r.f = ((r.h) != 0) ? 0 : 0x80;
            r.f = (r.f & 0xEF) + co;
            r.m = 2;
        }

        public void SLLr_l() {
            int co = ((r.l & 0x80) != 0) ? 0x10 : 0;
            r.l = (r.l << 1) & 255 + 1;
            r.f = ((r.l) != 0) ? 0 : 0x80;
            r.f = (r.f & 0xEF) + co;
            r.m = 2;
        }

        public void SLLr_a() {
            int co = ((r.a & 0x80) != 0) ? 0x10 : 0;
            r.a = (r.a << 1) & 255 + 1;
            r.f = ((r.a) != 0) ? 0 : 0x80;
            r.f = (r.f & 0xEF) + co;
            r.m = 2;
        }

        public void SRAr_b() {
            int ci = ((r.b & 0x80));
            int co = ((r.b & 1) != 0) ? 0x10 : 0;
            r.b = ((r.b >> 1) + ci) & 255;
            r.f = ((r.b) != 0) ? 0 : 0x80;
            r.f = (r.f & 0xEF) + co;
            r.m = 2;
        }

        public void SRAr_c() {
            int ci = ((r.c & 0x80));
            int co = ((r.c & 1) != 0) ? 0x10 : 0;
            r.c = ((r.c >> 1) + ci) & 255;
            r.f = ((r.c) != 0) ? 0 : 0x80;
            r.f = (r.f & 0xEF) + co;
            r.m = 2;
        }

        public void SRAr_d() {
            int ci = ((r.d & 0x80));
            int co = ((r.d & 1) != 0) ? 0x10 : 0;
            r.d = ((r.d >> 1) + ci) & 255;
            r.f = ((r.d) != 0) ? 0 : 0x80;
            r.f = (r.f & 0xEF) + co;
            r.m = 2;
        }

        public void SRAr_e() {
            int ci = ((r.e & 0x80));
            int co = ((r.e & 1) != 0) ? 0x10 : 0;
            r.e = ((r.e >> 1) + ci) & 255;
            r.f = ((r.e) != 0) ? 0 : 0x80;
            r.f = (r.f & 0xEF) + co;
            r.m = 2;
        }

        public void SRAr_h() {
            int ci = ((r.h & 0x80));
            int co = ((r.h & 1) != 0) ? 0x10 : 0;
            r.h = ((r.h >> 1) + ci) & 255;
            r.f = ((r.h) != 0) ? 0 : 0x80;
            r.f = (r.f & 0xEF) + co;
            r.m = 2;
        }

        public void SRAr_l() {
            int ci = ((r.l & 0x80));
            int co = ((r.l & 1) != 0) ? 0x10 : 0;
            r.l = ((r.l >> 1) + ci) & 255;
            r.f = ((r.l) != 0) ? 0 : 0x80;
            r.f = (r.f & 0xEF) + co;
            r.m = 2;
        }

        public void SRAr_a() {
            int ci = ((r.a & 0x80));
            int co = ((r.a & 1) != 0) ? 0x10 : 0;
            r.a = ((r.a >> 1) + ci) & 255;
            r.f = ((r.a) != 0) ? 0 : 0x80;
            r.f = (r.f & 0xEF) + co;
            r.m = 2;
        }

        public void SRLr_b() {
            int co = ((r.b & 1) != 0) ? 0x10 : 0;
            r.b = (r.b >> 1) & 255;
            r.f = ((r.b) != 0) ? 0 : 0x80;
            r.f = (r.f & 0xEF) + co;
            r.m = 2;
        }

        public void SRLr_c() {
            int co = ((r.c & 1) != 0) ? 0x10 : 0;
            r.c = (r.c >> 1) & 255;
            r.f = ((r.c) != 0) ? 0 : 0x80;
            r.f = (r.f & 0xEF) + co;
            r.m = 2;
        }

        public void SRLr_d() {
            int co = ((r.d & 1) != 0) ? 0x10 : 0;
            r.d = (r.d >> 1) & 255;
            r.f = ((r.d) != 0) ? 0 : 0x80;
            r.f = (r.f & 0xEF) + co;
            r.m = 2;
        }

        public void SRLr_e() {
            int co = ((r.e & 1) != 0) ? 0x10 : 0;
            r.e = (r.e >> 1) & 255;
            r.f = ((r.e) != 0) ? 0 : 0x80;
            r.f = (r.f & 0xEF) + co;
            r.m = 2;
        }

        public void SRLr_h() {
            int co = ((r.h & 1) != 0) ? 0x10 : 0;
            r.h = (r.h >> 1) & 255;
            r.f = ((r.h) != 0) ? 0 : 0x80;
            r.f = (r.f & 0xEF) + co;
            r.m = 2;
        }

        public void SRLr_l() {
            int co = ((r.l & 1) != 0) ? 0x10 : 0;
            r.l = (r.l >> 1) & 255;
            r.f = ((r.l) != 0) ? 0 : 0x80;
            r.f = (r.f & 0xEF) + co;
            r.m = 2;
        }

        public void SRLr_a() {
            int co = ((r.a & 1) != 0) ? 0x10 : 0;
            r.a = (r.a >> 1) & 255;
            r.f = ((r.a) != 0) ? 0 : 0x80;
            r.f = (r.f & 0xEF) + co;
            r.m = 2;
        }

        public void CPL() {
            r.a ^= 255;
            r.f = (r.a != 0) ? 0 : 0x80;
            r.m = 1;
        }

        public void NEG() {
            r.a = 0 - r.a;
            r.f = (r.a < 0) ? 0x10 : 0;
            r.a &= 255;
            if (r.a == 0)
                r.f |= 0x80;
            r.m = 2;
        }

        public void CCF() {
            int ci = ((r.f & 0x10) != 0) ? 0 : 0x10;
            r.f = (r.f & 0xEF) + ci;
            r.m = 1;
        }

        public void SCF() {
            r.f |= 0x10;
            r.m = 1;
        }

        /*--- Stack ---*/
        public void PUSHBC() {
            r.sp--;
            mmu.wb(r.sp, r.b);
            r.sp--;
            mmu.wb(r.sp, r.c);
            r.m = 3;
        }

        public void PUSHDE() {
            r.sp--;
            mmu.wb(r.sp, r.d);
            r.sp--;
            mmu.wb(r.sp, r.e);
            r.m = 3;
        }

        public void PUSHHL() {
            r.sp--;
            mmu.wb(r.sp, r.h);
            r.sp--;
            mmu.wb(r.sp, r.l);
            r.m = 3;
        }

        public void PUSHAF() {
            r.sp--;
            mmu.wb(r.sp, r.a);
            r.sp--;
            mmu.wb(r.sp, r.f);
            r.m = 3;
        }

        public void POPBC() {
            r.c = mmu.rb(r.sp);
            r.sp++;
            r.b = mmu.rb(r.sp);
            r.sp++;
            r.m = 3;
        }

        public void POPDE() {
            r.e = mmu.rb(r.sp);
            r.sp++;
            r.d = mmu.rb(r.sp);
            r.sp++;
            r.m = 3;
        }

        public void POPHL() {
            r.l = mmu.rb(r.sp);
            r.sp++;
            r.h = mmu.rb(r.sp);
            r.sp++;
            r.m = 3;
        }

        public void POPAF() {
            r.f = mmu.rb(r.sp);
            r.sp++;
            r.a = mmu.rb(r.sp);
            r.sp++;
            r.m = 3;
        }

        /*--- Jump ---*/
        public void JPnn() {
            r.pc = mmu.rw(r.pc);
            r.m = 3;
        }

        public void JPHL() {
            r.pc = (r.h << 8) + r.l;
            r.m = 1;
        }

        public void JPNZnn() {
            r.m = 3;
            if ((r.f & 0x80) == 0x00) {
                r.pc = mmu.rw(r.pc);
                r.m++;
            } else
                r.pc += 2;
        }

        public void JPZnn() {
            r.m = 3;
            if ((r.f & 0x80) == 0x80) {
                r.pc = mmu.rw(r.pc);
                r.m++;
            } else
                r.pc += 2;
        }

        public void JPNCnn() {
            r.m = 3;
            if ((((r.f & 0x10))) == 0x00) {
                r.pc = mmu.rw(r.pc);
                r.m++;
            } else
                r.pc += 2;
        }

        public void JPCnn() {
            r.m = 3;
            if ((((r.f & 0x10))) == 0x10) {
                r.pc = mmu.rw(r.pc);
                r.m++;
            } else
                r.pc += 2;
        }

        public void JRn() {
            int i = mmu.rb(r.pc);
            if (i > 127)
                i = -((~i + 1) & 255);
            r.pc++;
            r.m = 2;
            r.pc += i;
            r.m++;
        }

        public void JRNZn() {
            int i = mmu.rb(r.pc);
            if (i > 127)
                i = -((~i + 1) & 255);
            r.pc++;
            r.m = 2;
            if ((r.f & 0x80) == 0x00) {
                r.pc += i;
                r.m++;
            }
        }

        public void JRZn() {
            int i = mmu.rb(r.pc);
            if (i > 127)
                i = -((~i + 1) & 255);
            r.pc++;
            r.m = 2;
            if ((r.f & 0x80) == 0x80) {
                r.pc += i;
                r.m++;
            }
        }

        public void JRNCn() {
            int i = mmu.rb(r.pc);
            if (i > 127)
                i = -((~i + 1) & 255);
            r.pc++;
            r.m = 2;
            if (((r.f & 0x10)) == 0x00) {
                r.pc += i;
                r.m++;
            }
        }

        public void JRCn() {
            int i = mmu.rb(r.pc);
            if (i > 127)
                i = -((~i + 1) & 255);
            r.pc++;
            r.m = 2;
            if (((r.f & 0x10)) == 0x10) {
                r.pc += i;
                r.m++;
            }
        }

        public void DJNZn() {
            int i = mmu.rb(r.pc);
            if (i > 127)
                i = -((~i + 1) & 255);
            r.pc++;
            r.m = 2;
            r.b--;
            if ((r.b) != 0) {
                r.pc += i;
                r.m++;
            }
        }

        public void CALLnn() {
            r.sp -= 2;
            mmu.ww(r.sp, r.pc + 2);
            r.pc = mmu.rw(r.pc);
            r.m = 5;
        }

        public void CALLNZnn() {
            r.m = 3;
            if ((r.f & 0x80) == 0x00) {
                r.sp -= 2;
                mmu.ww(r.sp, r.pc + 2);
                r.pc = mmu.rw(r.pc);
                r.m += 2;
            } else
                r.pc += 2;
        }

        public void CALLZnn() {
            r.m = 3;
            if ((r.f & 0x80) == 0x80) {
                r.sp -= 2;
                mmu.ww(r.sp, r.pc + 2);
                r.pc = mmu.rw(r.pc);
                r.m += 2;
            } else
                r.pc += 2;
        }

        public void CALLNCnn() {
            r.m = 3;
            if ((((r.f & 0x10))) == 0x00) {
                r.sp -= 2;
                mmu.ww(r.sp, r.pc + 2);
                r.pc = mmu.rw(r.pc);
                r.m += 2;
            } else
                r.pc += 2;
        }

        public void CALLCnn() {
            r.m = 3;
            if ((((r.f & 0x10))) == 0x10) {
                r.sp -= 2;
                mmu.ww(r.sp, r.pc + 2);
                r.pc = mmu.rw(r.pc);
                r.m += 2;
            } else
                r.pc += 2;
        }

        public void RET() {
            r.pc = mmu.rw(r.sp);
            r.sp += 2;
            r.m = 3;
        }

        public void RETI() {
            r.ime = 1;
            ops.rrs();
            r.pc = mmu.rw(r.sp);
            r.sp += 2;
            r.m = 3;
        }

        public void RETNZ() {
            r.m = 1;
            if ((r.f & 0x80) == 0x00) {
                r.pc = mmu.rw(r.sp);
                r.sp += 2;
                r.m += 2;
            }
        }

        public void RETZ() {
            r.m = 1;
            if ((r.f & 0x80) == 0x80) {
                r.pc = mmu.rw(r.sp);
                r.sp += 2;
                r.m += 2;
            }
        }

        public void RETNC() {
            r.m = 1;
            if ((((r.f & 0x10))) == 0x00) {
                r.pc = mmu.rw(r.sp);
                r.sp += 2;
                r.m += 2;
            }
        }

        public void RETC() {
            r.m = 1;
            if ((((r.f & 0x10))) == 0x10) {
                r.pc = mmu.rw(r.sp);
                r.sp += 2;
                r.m += 2;
            }
        }

        public void RST00() {
            ops.rsv();
            r.sp -= 2;
            mmu.ww(r.sp, r.pc);
            r.pc = 0x00;
            r.m = 3;
        }

        public void RST08() {
            ops.rsv();
            r.sp -= 2;
            mmu.ww(r.sp, r.pc);
            r.pc = 0x08;
            r.m = 3;
        }

        public void RST10() {
            ops.rsv();
            r.sp -= 2;
            mmu.ww(r.sp, r.pc);
            r.pc = 0x10;
            r.m = 3;
        }

        public void RST18() {
            ops.rsv();
            r.sp -= 2;
            mmu.ww(r.sp, r.pc);
            r.pc = 0x18;
            r.m = 3;
        }

        public void RST20() {
            ops.rsv();
            r.sp -= 2;
            mmu.ww(r.sp, r.pc);
            r.pc = 0x20;
            r.m = 3;
        }

        public void RST28() {
            ops.rsv();
            r.sp -= 2;
            mmu.ww(r.sp, r.pc);
            r.pc = 0x28;
            r.m = 3;
        }

        public void RST30() {
            ops.rsv();
            r.sp -= 2;
            mmu.ww(r.sp, r.pc);
            r.pc = 0x30;
            r.m = 3;
        }

        public void RST38() {
            ops.rsv();
            r.sp -= 2;
            mmu.ww(r.sp, r.pc);
            r.pc = 0x38;
            r.m = 3;
        }

        public void RST40() {
            ops.rsv();
            r.sp -= 2;
            mmu.ww(r.sp, r.pc);
            r.pc = 0x40;
            r.m = 3;
        }

        public void RST48() {
            ops.rsv();
            r.sp -= 2;
            mmu.ww(r.sp, r.pc);
            r.pc = 0x48;
            r.m = 3;
        }

        public void RST50() {
            ops.rsv();
            r.sp -= 2;
            mmu.ww(r.sp, r.pc);
            r.pc = 0x50;
            r.m = 3;
        }

        public void RST58() {
            ops.rsv();
            r.sp -= 2;
            mmu.ww(r.sp, r.pc);
            r.pc = 0x58;
            r.m = 3;
        }

        public void RST60() {
            ops.rsv();
            r.sp -= 2;
            mmu.ww(r.sp, r.pc);
            r.pc = 0x60;
            r.m = 3;
        }

        public void NOP() {
            r.m = 1;
        }

        public void HALT() {
            halt = true;
            r.m = 1;
        }

        public void DI() {
            r.ime = 0;
            r.m = 1;
        }

        public void EI() {
            r.ime = 1;
            r.m = 1;
        }

        /*--- Helper s ---*/
        public void rsv() {
            rsv.a = r.a;
            rsv.b = r.b;
            rsv.c = r.c;
            rsv.d = r.d;
            rsv.e = r.e;
            rsv.f = r.f;
            rsv.h = r.h;
            rsv.l = r.l;
        }

        public void rrs() {
            r.a = rsv.a;
            r.b = rsv.b;
            r.c = rsv.c;
            r.d = rsv.d;
            r.e = rsv.e;
            r.f = rsv.f;
            r.h = rsv.h;
            r.l = rsv.l;
        }

        public void MAPcb() {
            int ins = mmu.rb(r.pc);
            r.pc++;
            r.pc &= 65535;

            if (ins >= map.length) {
                System.out.println("Unknown instruction: " + ins);
            } else {
                try {
                    ops.getClass().getMethod(cbmap[ins]).invoke(ops);
                } catch (Exception e) {
                    // TODO: handle exception
                    System.out.println("Failed to execute instruction: " + ins);
                }
            }

            // if(cbmap[i]) cbmap[i]();
            // else System.out.println((i));
        }

        public void XX() {
            var opc = r.pc-1;
            System.out.println("Unimplemented instruction ("+opc+"). stopping.");
            stop=true;
        }

    }

    OPS ops = new OPS();
}