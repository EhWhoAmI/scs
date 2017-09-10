package scstools;


/**
 * Write a description of interface command here.
 * 
 * @author Zyun
 * @version 0.0.0
 */
public interface Command
{
    int EXIT_SUCCESS = 0;
    int EXIT_FAILURE = 1;
    public int execute(String[] args);
}
