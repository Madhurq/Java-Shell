package main;

import java.util.ArrayList;
import java.util.List;

public class Parsec
{
    public static String[] parse(String command)
    {
        List<String> pl = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean singleq = false;
        boolean doubleq = false;
        for (int i = 0; i < command.length(); i++)
        {
            char c = command.charAt(i);
            if (c == '\\')
            {
                if (singleq)
                {
                    cur.append('\\');
                }
                else if (doubleq)
                {
                    if (i + 1 < command.length())
                    {
                        char next = command.charAt(++i);
                        if (next == '\"' || next == '\\' || next == '$' || next == '`')
                        {
                            cur.append(next);
                        }
                        else
                        {
                            cur.append('\\').append(next); // keep the backslash
                        }
                    }
                    else
                    {
                        cur.append('\\');
                    }
                }
                else
                {
                    // outside quotes
                    if (i + 1 < command.length())
                    {
                        char next = command.charAt(++i);
                        cur.append(next);
                    }
                    else
                    {
                        cur.append("\\");
                    }
                }
            }
            else if (c == ' ' && !singleq && !doubleq)
            {
                if (!cur.isEmpty())
                {
                    pl.add(cur.toString());
                    cur.setLength(0);
                }
            }
            else if (c == '\'' && !doubleq) // this is the java notation for a single character '
            {
                singleq = !singleq;
            }
            else if (c == '\"' && !singleq) // this is the java notation for a double character "
            {
                doubleq = !doubleq;
            }
            else
            {
                cur.append(c);
            }
        }
        if (!cur.isEmpty())
        {
            pl.add(cur.toString());
        }
        return pl.toArray(String[]::new);
    }
}
