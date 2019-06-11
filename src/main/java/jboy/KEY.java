package jboy;

public class KEY {

    int[] keys = new int[] { 0x0f, 0x0f };
    int colidx = 0;

    public void reset() {
        keys = new int[] { 0x0f, 0x0f };
        colidx = 0;
    }

    public int rb() {
        switch (colidx) {
        case 0x00:
            return 0x00;
        case 0x10:
            return keys[0];
        case 0x20:
            return keys[1];
        default:
            return 0x00;
        }
    }

    public void wb(int val) {
        colidx = val & 0x30;
    }

    public void keyDown(int code) {
        switch (code) {
        case 39:
            keys[1] &= 0xE;
            break;
        case 37:
            keys[1] &= 0xD;
            break;
        case 38:
            keys[1] &= 0xB;
            break;
        case 40:
            keys[1] &= 0x7;
            break;
        case 90:
            keys[0] &= 0xE;
            break;
        case 88:
            keys[0] &= 0xD;
            break;
        case 32:
            keys[0] &= 0xB;
            break;
        case 13:
            keys[0] &= 0x7;
            break;
        }
    }

    public void keyUp(int code) {
        switch (code) {
        case 39:
            keys[1] |= 0x1;
            break;
        case 37:
            keys[1] |= 0x2;
            break;
        case 38:
            keys[1] |= 0x4;
            break;
        case 40:
            keys[1] |= 0x8;
            break;
        case 90:
            keys[0] |= 0x1;
            break;
        case 88:
            keys[0] |= 0x2;
            break;
        case 32:
            keys[0] |= 0x5;
            break;
        case 13:
            keys[0] |= 0x8;
            break;
        }
    }
}