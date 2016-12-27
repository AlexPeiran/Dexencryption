package com.example.reforceapk;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class FileUtils {
	// �����ļ�  
    public static void saveFile(String newsRootPath, String filename,  
            File picFile) {  
        try {  
            File newsFileRoot = new File(newsRootPath);  
            if (!newsFileRoot.exists()) {  
                newsFileRoot.mkdirs();  
            }  
  
            FileOutputStream fos = new FileOutputStream(newsRootPath + filename);  
            FileInputStream fis = new FileInputStream(picFile);  
            byte[] buf = new byte[1024];  
            int len = 0;  
            while ((len = fis.read(buf)) > 0) {  
                fos.write(buf, 0, len);  
            }  
            if (fis != null)  
                fis.close();  
            if (fos != null)  
                fos.close();  
        } catch (Exception ex) {  
            ex.printStackTrace();  
        }  
    }  
  
  
    // ɾ���ļ�  
    public static boolean deleteFile(String filePath) {  
        boolean flag = false;  
        File file = new File(filePath);  
        // ·��Ϊ�ļ��Ҳ�Ϊ�������ɾ��  
        if (file.isFile() && file.exists()) {  
            file.delete();  
            flag = true;  
        }  
        return flag;  
    }  
  
    // ɾ���ļ���Ŀ¼  
    public static boolean deleteFolder(String filePath) {  
        boolean flag = false;  
        File file = new File(filePath);  
        // �ж�Ŀ¼���ļ��Ƿ����  
        if (!file.exists()) { // �����ڷ��� false  
            return flag;  
        } else {  
            // �ж��Ƿ�Ϊ�ļ�  
            if (file.isFile()) { // Ϊ�ļ�ʱ����ɾ���ļ�����  
                return deleteFile(filePath);  
            } else { // ΪĿ¼ʱ����ɾ��Ŀ¼����  
                return deleteDirectory(filePath);  
            }  
        }  
    }  
  
    // ɾ��Ŀ¼  
    public static boolean deleteDirectory(String filePath) {  
        boolean flag = false;  
        // ���sPath�����ļ��ָ�����β���Զ�����ļ��ָ���  
        if (!filePath.endsWith(File.separator)) {  
            filePath = filePath + File.separator;  
        }  
        File dirFile = new File(filePath);  
        // ���dir��Ӧ���ļ������ڣ����߲���һ��Ŀ¼�����˳�  
        if (!dirFile.exists() || !dirFile.isDirectory()) {  
            return false;  
        }  
        flag = true;  
        // ɾ���ļ����µ������ļ�(������Ŀ¼)  
        File[] files = dirFile.listFiles();  
        for (int i = 0; i < files.length; i++) {  
            // ɾ�����ļ�  
            if (files[i].isFile()) {  
                flag = deleteFile(files[i].getAbsolutePath());  
                if (!flag)  
                    break;  
            } // ɾ����Ŀ¼  
            else {  
                flag = deleteDirectory(files[i].getAbsolutePath());  
                if (!flag)  
                    break;  
            }  
        }  
        if (!flag)  
            return false;  
        // ɾ����ǰĿ¼  
        if (dirFile.delete()) {  
            return true;  
        } else {  
            return false;  
        }  
    }
}
