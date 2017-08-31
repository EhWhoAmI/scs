package scstools;


/**
 * Write a description of interface command here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public interface Command
{
    int EXIT_SUCCESS = 0;
    public int execute(String[] args);
}
