package fr.an.screencast.compressor.utils;

public enum QuadDirection { 
    RIGHT, UP, LEFT, DOWN;

    public void shiftPt(Pt pt) {
        switch(this) {
        case RIGHT: pt.x++; break;
        case UP: pt.y--; break;
        case LEFT: pt.x--; break;
        case DOWN: pt.y++; break;            
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