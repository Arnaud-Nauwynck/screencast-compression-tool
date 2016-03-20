package fr.an.screencast.compressor.utils;

public enum QuadDirection { 
    RIGHT, UP, LEFT, DOWN;
    
    public static QuadDirection of(int dir) {
        switch(dir) {
        case 0: return RIGHT;
        case 1: return UP;
        case 2: return LEFT;
        case 3: return DOWN;
        default: throw new IllegalArgumentException();
        }
    }

    public Pt newShiftPt(Pt pt) {
        Pt res = new Pt(pt); 
        shiftPt(res);
        return res;
    }
    
    public void shiftPt(Pt pt) {
        switch(this) {
        case RIGHT: pt.x++; break;
        case UP: pt.y--; break;
        case LEFT: pt.x--; break;
        case DOWN: pt.y++; break;            
        }
    }

    public int shiftPtIdx(int idx, int W) {
        switch(this) {
        case RIGHT: return idx+1;
        case UP: return idx-W;
        case LEFT: return idx-1;
        case DOWN: return idx+W;
        default: throw new IllegalArgumentException();
        }
    }

    public QuadDirection nextCyclicRightLeftDownDirection() {
        switch(this) {
        case RIGHT: return LEFT;
        case UP: return LEFT;// should not occur
        case LEFT: return DOWN;
        case DOWN: return RIGHT;            
        default: return LEFT; // can not occur
        }
    }

    public QuadDirection nextClockwise() {
        switch(this) {
        case RIGHT: return DOWN;
        case UP: return RIGHT;
        case LEFT: return UP;
        case DOWN: return LEFT;            
        default: return LEFT; // can not occur
        }
    }

}