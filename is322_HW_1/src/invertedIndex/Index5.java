/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package invertedIndex;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Writer;
import java.io.IOException;
import java.io.InputStreamReader;
import static java.lang.Math.log10;
import static java.lang.Math.sqrt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.io.PrintWriter;

/**
 *
 * @author ehab
 */
// Constructor to initialize data structures
public class Index5 {

    //--------------------------------------------
    int N = 0;
    public Map<Integer, SourceRecord> sources;  // store the doc_id and the file name.

    public HashMap<String, DictEntry> index; // THe inverted index
    //--------------------------------------------

    public Index5() {
        sources = new HashMap<Integer, SourceRecord>();
        index = new HashMap<String, DictEntry>();
    }
    // Setter method for N
    public void setN(int n) {
        N = n;
    }


    //---------------------------------------------
    //  print the posting list for a term
    public void printPostingList(Posting p) 
    {
        // Iterator<Integer> it2 = hset.iterator();
        System.out.print("[");
        boolean first = true;
        while (p != null) 
        {
            if (!first) //4- grid last comma
            {
                System.out.print(",");
            } else
            {
                first = false;
            }
            System.out.print("" + p.docId );
            p = p.next;
        }
        System.out.println("]");
    }

    //---------------------------------------------
    //  print the entire dictionary
    public void printDictionary() 
    {
        Iterator it = index.entrySet().iterator();
        while (it.hasNext())
        {
            Map.Entry pair = (Map.Entry) it.next();
            DictEntry dd = (DictEntry) pair.getValue();
            System.out.print("** [" + pair.getKey() + "," + dd.doc_freq + "]       =--> ");
            printPostingList(dd.pList);
        }
        System.out.println("------------------------------------------------------");
        System.out.println("*** Number of terms = " + index.size());
    }
 
    //-----------------------------------------------
    // 2- build the inverted index from disk
    public void buildIndex(String[] files) {  // from disk not from the internet
        int fid = 0; //unique id to each document processed.
        for (String fileName : files) {
            try (BufferedReader file = new BufferedReader(new FileReader(fileName))) {
                if (!sources.containsKey(fileName)) {
                    sources.put(fid, new SourceRecord(fid, fileName, fileName, "notext"));
                }
                String ln;
                int flen = 0;
                while ((ln = file.readLine()) != null) 
                {
                    flen += indexOneLine(ln, fid); // Update document length
                }
                sources.get(fid).length = flen;

            } catch (IOException e) {
                System.out.println("File " + fileName + " not found. Skip it");
            }
            fid++;
        }
           printDictionary();
    }

    //----------------------------------------------------------------------------
    //  index one line of text
    public int indexOneLine(String ln, int fid) 
    {
        int flen = 0;

        String[] words = ln.split("\\W+");
      // String[] words = ln.replaceAll("(?:[^a-zA-Z0-9 -]|(?<=\\w)-(?!\\S))", " ").toLowerCase().split("\\s+");
        flen += words.length;
        for (String word : words) 
        {
            word = word.toLowerCase();
            if (stopWord(word)) 
            {
                continue;
            }
            word = stemWord(word);
            // check to see if the word is not in the dictionary
            // if not add it
            if (!index.containsKey(word)) {
                index.put(word, new DictEntry());
            }
            // add document id to the posting list
            if (!index.get(word).postingListContains(fid)) {
                index.get(word).doc_freq += 1; //set doc freq to the number of doc that contain the term 
                if (index.get(word).pList == null) {
                    index.get(word).pList = new Posting(fid);
                    index.get(word).last = index.get(word).pList;
                } else {
                    index.get(word).last.next = new Posting(fid);
                    index.get(word).last = index.get(word).last.next;
                }
            } 
            else 
            {
                index.get(word).last.dtf += 1;
            }
            //set the term_fteq in the collection
            index.get(word).term_freq += 1;
            if (word.equalsIgnoreCase("lattice"))
             {

                System.out.println("  <<" + index.get(word).getPosting(1) + ">> " + ln);
            }

        }
        return flen;
    }

//----------------------------------------------------------------------------
//  check if a word is a stop word
    boolean stopWord(String word) 
    {
        if (word.equals("the") || word.equals("to") || word.equals("be") || word.equals("for") || word.equals("from") || word.equals("in")
                || word.equals("a") || word.equals("into") || word.equals("by") || word.equals("or") || word.equals("and") || word.equals("that")) {
            return true;
        }
        if (word.length() < 2) {
            return true;
        }
        return false;

    }
//----------------------------------------------------------------------------
    //  stem a word (currently not implemented)
    String stemWord(String word) { //skip for now
        return word;
//        Stemmer s = new Stemmer();
//        s.addString(word);
//        s.stem();
//        return s.toString();
    }

    //----------------------------------------------------------------------------
    //1- intersect two posting lists
    Posting intersect(Posting pL1, Posting pL2) 
    {
        Posting answer = null; //like pointer on a head 
        Posting last = null;// like ointer on last

        while (pL1!= null && pL2!=null) 
        {
            if (pL1.docId== pL2.docId) 
            {
                if (answer== null) 
                {
                    answer=new Posting(pL1.docId);
                    last=answer;
                }
                else 
                {
                    last.next = new Posting(pL1.docId);
                    last=last.next;
                }
                pL1=pL1.next;
                pL2=pL2.next;
            }
            else if (pL1.docId<pL2.docId) 
                pL1 = pL1.next;
            else 
                pL2 = pL2.next;
            
        }
        return answer;
    }
    //  find documents containing a given phrase
    public String find_24_01(String phrase) { // any mumber of terms non-optimized search 
        String result = "";
        String[] words = phrase.split("\\W+");
        int len = words.length;
        
        //fix this if word is not in the hash table will crash...
        Posting posting = index.get(words[0].toLowerCase()).pList;
        int i = 1;
        while (i < len) {
            posting = intersect(posting, index.get(words[i].toLowerCase()).pList);
            i++;
        }
        while (posting != null) {
            //System.out.println("\t" + sources.get(num));
            result += "\t" + posting.docId + " - " + sources.get(posting.docId).title + " - " + sources.get(posting.docId).length + "\n";
            posting = posting.next;
        }
        return result;
    }
    
    
    //---------------------------------
    String[] sort(String[] words) {  //bubble sort
        boolean sorted = false;
        String sTmp;
        //-------------------------------------------------------
        while (!sorted) {
            sorted = true;
            for (int i = 0; i < words.length - 1; i++) {
                int compare = words[i].compareTo(words[i + 1]);
                if (compare > 0) {
                    sTmp = words[i];
                    words[i] = words[i + 1];
                    words[i + 1] = sTmp;
                    sorted = false;
                }
            }
        }
        return words;
    }

     //---------------------------------

    //sort an array of strings (Bubble Sort)
    public void store(String storageName) {
        try {
            String pathToStorage = "D:\\University\\Fourth Level 2'nd\\IR\\Assignments\\S1-2_20200425_20201043_20201217_20201120_20200442\\tmp11\\rl"+storageName;
            Writer wr = new FileWriter(pathToStorage);
            for (Map.Entry<Integer, SourceRecord> entry : sources.entrySet()) {
                System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue().URL + ", Value = " + entry.getValue().title + ", Value = " + entry.getValue().text);
                wr.write(entry.getKey().toString() + ",");
                wr.write(entry.getValue().URL.toString() + ",");
                wr.write(entry.getValue().title.replace(',', '~') + ",");
                wr.write(entry.getValue().length + ","); //String formattedDouble = String.format("%.2f", fee );
                wr.write(String.format("%4.4f", entry.getValue().norm) + ",");
                wr.write(entry.getValue().text.toString().replace(',', '~') + "\n");
            }
            wr.write("section2" + "\n");

            Iterator it = index.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                DictEntry dd = (DictEntry) pair.getValue();
                //  System.out.print("** [" + pair.getKey() + "," + dd.doc_freq + "] <" + dd.term_freq + "> =--> ");
                wr.write(pair.getKey().toString() + "," + dd.doc_freq + "," + dd.term_freq + ";");
                Posting p = dd.pList;
                while (p != null) {
                    //    System.out.print( p.docId + "," + p.dtf + ":");
                    wr.write(p.docId + "," + p.dtf + ":");
                    p = p.next;
                }
                wr.write("\n");
            }
            wr.write("end" + "\n");
            wr.close();
            System.out.println("=============EBD STORE=============");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
//=========================================
    //store the index and sources to a file
    public boolean storageFileExists(String storageName){
        java.io.File f = new java.io.File("D:\\University\\Fourth Level 2'nd\\IR\\Assignments\\S1-2_20200425_20201043_20201217_20201120_20200442\\tmp11\\rl"+storageName);
        if (f.exists() && !f.isDirectory())
            return true;
        return false;
            
    }
//----------------------------------------------------

    public void createStore(String storageName) {
        try {
            // Define the path to the storage file by appending the storageName to the directory path
            String pathToStorage = "D:\\University\\Fourth Level 2'nd\\IR\\Assignments\\S1-2_20200425_20201043_20201217_20201120_20200442\\tmp11" + storageName;

            // Create a new FileWriter to write to the storage file
            Writer wr = new FileWriter(pathToStorage);

            // Write "end" to the storage file to mark the end of content
            wr.write("end" + "\n");

            // Close the FileWriter to release resources
            wr.close();
        } catch (Exception e) {
            // Handle any exceptions that occur during file creation and writing
            e.printStackTrace();
        }
    }
//----------------------------------------------------      
     //load index from hard disk into memory
    /**
     * Loads the index and sources from the specified storage file.
     *
     * @param storageName The name of the storage file to load.
     * @return The loaded index as a HashMap of String keys (terms) and DictEntry values,
     *         containing the inverted index.
     */
    public HashMap<String, DictEntry> load(String storageName) {
        try {
            // Define the path to the storage file
            String pathToStorage = "D:\\University\\Fourth Level 2'nd\\IR\\Assignments\\S1-2_20200425_20201043_20201217_20201120_20200442\\tmp11\\rl" + storageName;

            // Initialize sources and index
            sources = new HashMap<Integer, SourceRecord>();
            index = new HashMap<String, DictEntry>();

            // Open the storage file for reading
            BufferedReader file = new BufferedReader(new FileReader(pathToStorage));
            String ln = "";
            int flen = 0;

            // Read and process lines until "section2" is encountered
            while ((ln = file.readLine()) != null) {
                if (ln.equalsIgnoreCase("section2")) {
                    break;
                }
                // Split the line by commas
                String[] ss = ln.split(",");
                // Extract information for SourceRecord creation
                int fid = Integer.parseInt(ss[0]);
                try {
                    // Create a SourceRecord object and add it to sources
                    SourceRecord sr = new SourceRecord(fid, ss[1], ss[2].replace('~', ','), Integer.parseInt(ss[3]), Double.parseDouble(ss[4]), ss[5].replace('~', ','));
                    sources.put(fid, sr);
                } catch (Exception e) {
                    System.out.println(fid + "  ERROR  " + e.getMessage());
                    e.printStackTrace();
                }
            }

            // Read and process lines containing the index data until "end" is encountered
            while ((ln = file.readLine()) != null) {
                if (ln.equalsIgnoreCase("end")) {
                    break;
                }
                // Split the line by semicolons to separate term info from posting list info
                String[] ss1 = ln.split(";");
                String[] ss1a = ss1[0].split(","); // Split term info
                String[] ss1b = ss1[1].split(":"); // Split posting list info

                // Create a new DictEntry for the term and add it to the index
                index.put(ss1a[0], new DictEntry(Integer.parseInt(ss1a[1]), Integer.parseInt(ss1a[2])));

                // Process each posting in the posting list
                for (int i = 0; i < ss1b.length; i++) {
                    // Split posting by commas to separate docId and dtf
                    String[] ss1bx = ss1b[i].split(",");
                    // Create a new Posting object
                    if (index.get(ss1a[0]).pList == null) {
                        // If the posting list for the term is empty, create a new one
                        index.get(ss1a[0]).pList = new Posting(Integer.parseInt(ss1bx[0]), Integer.parseInt(ss1bx[1]));
                        index.get(ss1a[0]).last = index.get(ss1a[0]).pList;
                    } else {
                        // If the posting list is not empty, append the new Posting
                        index.get(ss1a[0]).last.next = new Posting(Integer.parseInt(ss1bx[0]), Integer.parseInt(ss1bx[1]));
                        index.get(ss1a[0]).last = index.get(ss1a[0]).last.next;
                    }
                }
            }
            // Print a message indicating the end of the loading process
            System.out.println("============= END LOAD =============");
        } catch (Exception e) {
            // Print any exceptions that occur during the loading process
            e.printStackTrace();
        }
        // Return the loaded index
        return index;
    }

}

//=====================================================================
