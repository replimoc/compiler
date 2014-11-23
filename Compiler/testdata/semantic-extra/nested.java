class Nested
{
    public int correct1()
    {
        if(1 < 2)
        {
            while(3 > 0)
            {
                if(1 > 2)
                {
                    while (4 > 0)
                    {
                        if(1 > 3)
                        {
                            return 2;
                        }
                    }
                    return 1;
                }
            }
        }
        else
        {
            while(true)
            {
                return 5;
            }
        }
    }

    public int error1()
    {
        if(1 < 2)
        {
            while(3 > 0)
            {
                if(1 > 2)
                {
                    while (4 > 0)
                    {
                        if(1 > 3)
                        {
                            return 2;
                        }
                    }
                    return 1;
                }
            }
        }
        else
        {
            /* there is no return here */
        }
    }

    public int error2()
    {
        if(1 < 2)
        {
            /* there is no return here */
        }
        else
        {

            while(3 > 0)
            {
                if(1 > 2)
                {
                    while (4 > 0)
                    {
                        if(1 > 3)
                        {
                            return 2;
                        }
                    }
                    return 1;
                }
            }
        }
    }

    public int correct3()
    {
        while (1 < 2)
        {
            while (!false)
            {
                while (1 > 2)
                {
                    if(1 > 1)
                    {
                        return 0;
                    }
                    else
                    {
                        /* don't return anything */
                    }
                }
            }
        }

        return 1; /* here is return*/
    }

    public int error3()
    {
        while (1 < 2)
        {
            while (!false)
            {
                while (1 > 2)
                {
                    if(1 > 1)
                    {
                        return 0;
                    }
                    else
                    {
                        /* don't return anything */
                    }
                }
            }
        }
    }

    public static void main(String[] args) {

        /* */
        Nested n = new Nested();

    }
}