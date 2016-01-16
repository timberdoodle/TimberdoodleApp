package de.tudarmstadt.adtn.utility;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.ArrayList;

/**
 *
 */
public class FileWriter {

    public static String[] load(String path){
        try{
            File file = new File(path);
            BufferedReader br = new BufferedReader(new FileReader(file));
            ArrayList<String> lines = new ArrayList<>();
            String line;
            while ((line = br.readLine()) != null) lines.add(line);
            String[] result = new String[lines.size()];
            for(int i = 0; i < result.length; i++) result[i] = lines.get(i);
            return result;
        } catch (Exception e){
            return null;
        }
    }

    public static boolean delete(String path) {
        try {
            File file = new File(path);
            file.delete();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean write(String text, String path) {
        try {
            File file = new File(path);
            FileOutputStream fos = new FileOutputStream(file, true);
            fos.write(text.getBytes("UTF-8"));
            fos.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
