/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package encryption3;

import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 * @author Ben
 */
public class Thread4Encryption {

    /**
     * @param args the command line arguments
     */
    static class Permutations implements Runnable
    {
        String[] words;
        int count;
        int com;
        char startchar;
        char endchar;
        Permutations(int count1, int com1, char startLetter, char endLetter)
        {
            words = new String[com1];
            count = count1;
            com = com1;
            startchar = startLetter;
            endchar = endLetter;
        }
        @Override
        public void run(){
            char[] chars = new char[count];
            Arrays.fill(chars, startchar);
            final int last = count - 1;
            int k = 0;
            if(last == 0)
            {
                for(int i = 0; i < com; ++i)
                {
                    chars[0] = (char) (chars[0]+1);
                    words[i] = String.valueOf(chars);
                }
            }
            else
            {
                
                OUTER:
                while (true) {
                    for (chars[last] = 'a'; chars[last] <= 'z'; chars[last]+=1) {
                        if(k < com)
                        {
                            words[k] = String.valueOf(chars);
                            ++k;
                            chars[last]++;
                            words[k] = String.valueOf(chars);
                            ++k;
                        }
                    }

                    UPDATED:
                    {
                        for (int i = last - 1; i >= 0; --i) {
                            if (chars[i]++ >= 'z')
                                chars[i] = 'a';
                            else
                                break UPDATED;
                        }
                        // overflow;
                        break OUTER;
                    }
                }
            }
        }
    }
   static class Encrypt implements Runnable
    {
       String temp = new String();
       int startPos;
       int endPos;
       String[] EncryptionList;
       String[] keys;
       int difference;
       public Encrypt(String[] allWords, int start, int end)
       {
           this.difference = end-start;
           this.EncryptionList = new String[difference];
           this.keys = new String[difference];
           this.startPos = start;
           this.endPos = end;  
           int hash;
           for(int j = 0, l = this.startPos; j < this.difference; ++j)
           {
               hash = 5381;
               if(allWords[l] != null)
               {
                    temp = allWords[l];
                    for(int i = 0; i < temp.length(); ++i)
                    {
                         hash = ((hash << 5) + hash) + (temp.charAt(i)-'0');
                    }
                    this.keys[j] = Integer.toString(hash);
                    this.EncryptionList[j] = allWords[l];
                    ++l;
               }
           } 
        }
       @Override
       public void run() 
       {
            for(int p = 0, k = this.startPos; k < this.endPos; ++p, ++k)
            {
                if(this.EncryptionList[p] != null)
                {
                    String temp1 = this.EncryptionList[p];
                    String sb = "";
                    for (int i = 0, j = 0; i < temp1.length(); ++i, ++j) 
                    {
                        String key = this.keys[p];
                        if (j >= key.length()) j = 0;  
                        sb += ((char)(temp1.charAt(i) ^ key.charAt(j))); 
                    }
                    this.EncryptionList[p] = sb;
                }
            }
       }
    }
   static class BruteForceDecrypt implements Runnable
    {
        public String enPassword;
        boolean found;
        String[] enWords;
        String[] allWords;
        int startPos;
        
        public BruteForceDecrypt(String password1, boolean found1, String[] Enwords, String[] words) 
        {
            enPassword = password1;
            found = found1;
            enWords = Enwords;
            allWords = words;
            startPos = 0;
        }
        
        @Override
        public synchronized void run() 
        {
            int i = startPos;
            int j = 0;
            while(!found && j < enWords.length)
            {
                if(enWords[j] != null)
                {
                    if(enWords[j].equals(enPassword))
                    {
                        System.out.println("Word found: " + allWords[i]);
                        found = true;
                    }
                }
                ++i;
                ++j;
            }
        }
    }
    public static void main(String args[]) throws InterruptedException 
    {
        //Initialising key variables
        String[] password = new String[1];
        password[0] = "james";
        int count = password[0].length();
        final int com = (int)Math.pow(26, count);
        final int comOfWords = (com/26)*5;
        final int comOfWords1 = (com/26)*7;
        System.out.println("Origial Word: " + password[0]);
        //Encrypting password
        Encrypt e = new Encrypt(password, 0, 1);
        Thread t =  new Thread(e);
        t.start();
        t.join();
        String enPass = e.EncryptionList[0];
        System.out.println("Encrypted word: " + enPass);
        //Generate all permutations of a given length
        Permutations p1 = new Permutations(count, comOfWords, 'a', 'f');
        Permutations p2 = new Permutations(count, comOfWords1, 'f', 'm');
        Permutations p3 = new Permutations(count, comOfWords, 'm', 's');
        Permutations p4 = new Permutations(count, comOfWords1, 's', 'z');
        //Creating all threads for permutations
        Thread PermThread1 = new Thread(p1);
        Thread PermThread2 = new Thread(p2);
        Thread PermThread3 = new Thread(p3);
        Thread PermThread4 = new Thread(p4);
        //Pipeline attempt
        //T1 Create
        PermThread1.start();
        PermThread1.join();
        Encrypt e1 = new Encrypt(p1.words, 0, p1.words.length);
        BruteForceDecrypt B = new BruteForceDecrypt(enPass, false, e1.EncryptionList, p1.words);
        Thread t1 = new Thread(e1);
        Thread BFD1 = new Thread(B);
        //T1 Start E
        t1.start();
        //T2 Create
        PermThread2.start();
        PermThread2.join();
        Encrypt e2 = new Encrypt(p2.words, 0, p2.words.length);
        BruteForceDecrypt B1 = new BruteForceDecrypt(enPass, false, e2.EncryptionList, p2.words);
        Thread t2 = new Thread(e2);
        Thread BFD2 = new Thread(B1);
        //T1 end E start B
        t1.join();
        BFD1.start();
        //T2 Start E
        t2.start();
        //T3 create
        PermThread3.start();
        PermThread3.join();
        Encrypt e3 = new Encrypt(p3.words, 0, p3.words.length);
        BruteForceDecrypt B2 = new BruteForceDecrypt(enPass, false, e3.EncryptionList, p3.words);
        Thread t3 = new Thread(e3);
        Thread BFD3 = new Thread(B2);
        //T1 end B
        BFD1.join();
        if(!B.found)
        {
            //T2 end E start B
            t2.join();
            BFD2.start();
            //T3 start E
            t3.start();
            //T4 create
            PermThread4.start();
            PermThread4.join();
            Encrypt e4 = new Encrypt(p4.words, 0, p4.words.length);
            BruteForceDecrypt B3 = new BruteForceDecrypt(enPass, false, e4.EncryptionList, p4.words);
            Thread t4 = new Thread(e4);
            Thread BFD4 = new Thread(B3);
            //T2 end B
            BFD2.join();
            if(!B1.found)
            {
                //T3 end E start B
                t3.join();
                BFD3.start();
                //T4 start E
                t4.start();
                //T3 end B
                BFD3.join();
                if(!B2.found)
                {
                    //T4 end E start B
                    t4.join();
                    BFD4.start();
                    //T4 end B
                    BFD4.join();
                }
            }
        }
    }
}