package com.example.reforceapk;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringBufferInputStream;
import java.io.StringReader;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.Adler32;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * �ӿ�����
 * @author fplei
 *
 */
public class mymain {
	//����ָ����ǿ�apk��application
	private static String rename_application="com.handpay.shell.ProxyApplication";
	//ǩ����Կ����
	private static String key_store_name="test";
	//ǩ����Կ����·��
	private static String key_store_path="D:\\workspace\\NFCOEM\\"+key_store_name;
	//����
	private static String key_store_pwd="testtest";
	//����
	private static String key_aleas="testtest";
	/*private static String key_store_name="client-test.jks";
	private static String key_store_path="D:\\workspace\\trunk\\"+key_store_name;
	private static String key_store_pwd="client";
	private static String key_aleas="client-test";*/
	//�������ִ��ʱ��
	private static long UNPACKAGE_TIME=1000;
	//cmd�����˳�ʱ��
	private static long EXIT_CMD_TIME=1000;
	//�޸���Դʱ��
	private static long UPDATE_MANSIFEST_TIME=1000;
	//�ӿ�ʱ��
	private static long ENCRY_TIME=1000;
	//���ʱ��
	 private static long PACKAGE_TIME=1000;
	//ǩ��ʱ��
	private static long SINGER_TIME=2000;
	public enum OperationModle{
		ENCRY_DEX,
		ENCRY_APK,
		ENCRY_JAR
	}
	//��ǰ�ӿ�ģʽ
	private static OperationModle operation=OperationModle.ENCRY_DEX;
	//���·���ӿǺ�
	private static String apkoutputdir="E:\\output\\autoEncryapk";
	private static String parent_root=null;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String apk_file_path="force";
		getNeedEncrFile(apk_file_path);
	}
	
	//��ȡ�ͷ�apk�ļ�
	public static void getNeedEncrFile(String apk_file_path){
		File loadNeedEncFile = new File(apk_file_path);
		parent_root=loadNeedEncFile.getAbsolutePath();
		System.out.println("������Ŀ¼:"+parent_root);
		if(loadNeedEncFile.isDirectory()){
			File[] objfiles=loadNeedEncFile.listFiles();
			System.out.println("force�ļ�������"+objfiles.length+"���ļ���");
			for(File file:objfiles){
				System.out.println("�ļ�����"+file.getName()+",��С��"+file.length()+",·����"+file.getAbsolutePath());
				if(file.getName().contains(".apk")){
					updateProcessTime(file.length());
					DecoderApk(file);
				}
			}
			File loadNeedEncFile1 = new File(parent_root);
			//ɾ��δǩ�����ļ�
			for(File file1:loadNeedEncFile1.listFiles()){
				if(file1.getName().contains("unsing.apk")){
					FileUtils.deleteFile("force\\"+file1.getName());
				}
			}
		}
	}
	
	private static void updateProcessTime(long filesize){
		long size=(filesize/1024)/1024;
		if(size<=3){size=3;}
		UNPACKAGE_TIME=UNPACKAGE_TIME*size;
		ENCRY_TIME=ENCRY_TIME*size;
		PACKAGE_TIME=PACKAGE_TIME*size;
		SINGER_TIME=SINGER_TIME*size;
		System.out.println("UNPACKAGE_TIME:"+UNPACKAGE_TIME);
		System.out.println("ENCRY_TIME:"+ENCRY_TIME);
		System.out.println("PACKAGE_TIME:"+PACKAGE_TIME);
		System.out.println("SINGER_TIME:"+SINGER_TIME);
	}
	
	//������ 
	public static void DecoderApk(final File file){
		try{
			long starttime=System.currentTimeMillis();
			File outputfile=new File(parent_root+"\\"+file.getName().substring(0, file.getName().lastIndexOf(".")));
			System.out.println(outputfile.getAbsolutePath());
			// d ��ʾ����/b��ʾ���´��    -s ��ʾ������dex�ļ�  
//			String cmdUnpack = "tools\\apktool.jar d "+ file.getAbsolutePath()+" -o "+outputfile.getAbsolutePath();
			String cmdUnpack = "tools\\apktool.jar d -s "+ file.getAbsolutePath()+" -o "+outputfile.getAbsolutePath();
			System.out.println("����ִ�н������....");
			CMDUtils.runCMD(cmdUnpack);
			Thread.sleep(UNPACKAGE_TIME);
			long cmd_curr_time=System.currentTimeMillis();
			System.out.println(file.getName()+"------------->�����ɣ�");
			System.out.println(file.getName()+"------------->���ָ��ִ��ʱ�䣺"+(cmd_curr_time-starttime)+"ms");
			Thread.sleep(EXIT_CMD_TIME);
			CMDUtils.CMD("exit");//�ǵ��˳��������ļ��Ҳ���
			//�޸���Ŀ��Ϣ
			updateApkPkg(file);
			long cmd_curr_updatexml=System.currentTimeMillis();
			System.out.println("------------->AndroidManifest.xml�޸���ϣ���ʱ:"+(cmd_curr_updatexml-cmd_curr_time)+"ms");
			//�ӿ�
			String path="force\\"+file.getName();
			encry_dex(new File(path));
			long curr_encry_time=System.currentTimeMillis();
			System.out.println("------------->�ӿ�ʹ��ʱ�䣺"+(curr_encry_time-cmd_curr_updatexml)+"ms");
			
			//���´��
			System.out.println("------------->��ʼ���´��");
			String unsignApk = file.getName().substring(0, file.getName().lastIndexOf("."))+ "_jiagu_unsing.apk";
			String cmdpack="tools\\apktool.jar b force\\"+file.getName().substring(0, file.getName().lastIndexOf("."))+" -o force\\"+unsignApk;
			System.out.println("----->cmdpack:"+cmdpack);
			CMDUtils.runCMD(cmdpack);
			Thread.sleep(PACKAGE_TIME);
			long curr_pack_time=System.currentTimeMillis();
			System.out.println("------------->���´����ʱ��"+(curr_pack_time-curr_encry_time)+"ms");
			CMDUtils.CMD("exit");
			//ǩ��
			System.out.println("------------->��ʼ����ǩ��");
			String outputsingapkname=file.getName().substring(0, file.getName().lastIndexOf("."))+"_jiagu_sing.apk";
			String cmdsingapk="jarsigner -digestalg SHA1 -sigalg MD5withRSA -verbose -keystore "+key_store_path+" -storepass "+key_store_pwd+" -signedjar "+apkoutputdir+"\\"+outputsingapkname+" force\\"+unsignApk+" "+key_aleas;
			CMDUtils.runCMD(cmdsingapk);
			Thread.sleep(SINGER_TIME);
//			CMDUtils.CMD("exit");
			long curr_singapk_time=System.currentTimeMillis();
			System.out.println("------------->ǩ����ʱ��"+(curr_singapk_time-curr_pack_time)+"ms");
			//ɾ����apk  ��ֹ�ظ��ӿ�
//			finshed(file);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	//�޸İ���Ϣ
	public static void updateApkPkg(File file){
		//˳���libҲɾ��
		String libpathdirs="force\\"+file.getName().substring(0, file.getName().lastIndexOf("."))+"\\lib\\armeabi";
		String originalfile="libs";
		File libsfile=new File(originalfile);
		if(libsfile.exists()&&libsfile.isDirectory()){
			File[] libs=libsfile.listFiles();
			for(File obj:libs){
				try{
					System.out.println(obj.getPath());
					copySource(obj.getAbsolutePath(),libpathdirs);
				}catch(Exception e){
					System.out.println("����lib�ļ�����"+e.toString());
					return;
				}
			}
		}
		try{
		//�޸�AndroidManifest�ļ���Ϣ
		String mainfest_path="force\\"+file.getName().substring(0, file.getName().lastIndexOf("."))+"\\AndroidManifest.xml";
		System.out.println(mainfest_path);
		File manifest_file=new File(mainfest_path);
		if(!manifest_file.exists()){
			System.out.println("�����AndroidManifest.xml�ļ������ڣ�");
			return;
		}
		Document doc=XMLPares1.parse("force\\"+file.getName().substring(0, file.getName().lastIndexOf("."))+"\\AndroidManifest.xml");
		Element elt_root=doc.getDocumentElement();
		Element elt_application=XMLPares1.getChildElement(elt_root, "application");
		//String add_child_elt="<meta-data android:name=\"APPLICATION_CLASS_NAME\" android:value=\""+elt_application.getAttribute("android:name")+"\"/>";
		Element child_elt_tag=doc.createElement("meta-data");
			child_elt_tag.setAttribute("android:name", "APPLICATION_CLASS_NAME");
			String tag_application_value=elt_application.getAttribute("android:name");
			if(tag_application_value!=null&&tag_application_value.length()>0){
				child_elt_tag.setAttribute("android:value", tag_application_value);
			}else{
				child_elt_tag.setAttribute("android:value", "");
			}
			elt_application.appendChild(child_elt_tag);
		elt_application.setAttribute("android:name",rename_application);
		String update_xml =XMLPares1.doc2String(doc);
		writeString(mainfest_path,update_xml);
		
		}catch(Exception e){
			e.printStackTrace();
			return ;
		}
	}
	
	private static void copySource(String originalfile,String targetfile)throws Exception{
		System.out.println("originalfile:"+originalfile);
		File source=new File(originalfile);
		File dest=new File(targetfile+"\\"+source.getName());
		if(source.exists()){System.out.println("source����");}
		if(!dest.exists()){
			dest.createNewFile();	
		}
		if(dest.exists()){System.out.println("dest����");}
		InputStream input = null;     
		OutputStream output = null;     
		try {       
			input = new FileInputStream(source);      
			output = new FileOutputStream(dest);           
			byte[] buf = new byte[1024];           
			int bytesRead;          
			while ((bytesRead = input.read(buf)) > 0) {         
				output.write(buf, 0, bytesRead);       
			}  
		} finally {    
			input.close();     
			output.close();  
		} 
	}
	
	public static void finshed(File file){
		//ɾ��δǩ��apk

	}
	
	
	//<?xml version="1.0" encoding="utf-8"?>
	public static String read(String path) {  
        StringBuffer res = new StringBuffer();  
        String line = null;  
        try {  
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(path),"UTF-8"));  
            while ((line = reader.readLine()) != null) {  
            		res.append(line + "\n"); 
            }  
            reader.close();  
        } catch (FileNotFoundException e) {  
            e.printStackTrace();  
        } catch (IOException e) {  
            e.printStackTrace();  
        }  
        return res.toString();  
    }
	
	public static void writeString(String outpath,String context){
		try{
			File file=new File(outpath);
			if(file.exists()){
				file.delete();
			}
			String temp_context=context;
			FileOutputStream fileout=new FileOutputStream(outpath);
			byte[] temp_type=temp_context.getBytes(Charset.forName("UTF-8"));
			fileout.write(temp_type);
			fileout.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public static void encry_dex(File targetdex){
		try{
			System.out.println("----------------------����ʼ�ӿ�...."+targetdex.getAbsolutePath());
			
			File targetdexfile=null;
			if(operation==OperationModle.ENCRY_APK){
				targetdexfile = targetdex;   //��Ҫ�ӿǵĳ���apk
			}else if(operation==OperationModle.ENCRY_DEX){
				targetdexfile=new File("force\\"+targetdex.getName().substring(0, targetdex.getName().lastIndexOf("."))+"\\classes.dex");
			}
			if(targetdexfile.exists()){System.out.println("targetdexfile���ڣ�");}
			File otherdexfile=new File("force\\otherClasses.dex");//��dex
			byte[] targetdexArray = encrpt(readFileBytes(targetdexfile));//�Զ�������ʽ����apk�������м��ܴ���
			byte[] otherdexArray = readFileBytes(otherdexfile);//�Զ�������ʽ������dex
			int targetdexLen = targetdexArray.length;
			System.out.println("---------����Ҫ���ܵ�dex����"+targetdexLen);
			int otherdexLen = otherdexArray.length;
			System.out.println("---------����dex����"+otherdexLen);
			int totalLen = targetdexLen + otherdexLen +4;//���4�ֽ��Ǵ�ų��ȵġ�
			byte[] newdex = new byte[totalLen]; // �������µĳ���
			System.arraycopy(otherdexArray, 0, newdex, 0, otherdexLen);//�ȿ���dex����
			System.arraycopy(targetdexArray, 0, newdex, otherdexLen, targetdexLen);//���Դdex
			System.arraycopy(intToByte(targetdexLen), 0, newdex, totalLen-4, 4);//���4Ϊ����
			//�޸ĺϲ�֮��DEX file size�ļ�ͷ
			fixFileSizeHeader(newdex);
			//�޸ĺϲ�֮��DEX SHA1 �ļ�ͷ
			fixSHA1Header(newdex);
			//�޸ĺϲ�֮��DEX CheckSum�ļ�ͷ
			fixCheckSumHeader(newdex);
			String temp_outputpath="force\\"+targetdex.getName().substring(0, targetdex.getName().lastIndexOf("."))+"\\classes.dex";
			System.out.println("----------------------����dex���·����"+temp_outputpath);
			File file = new File(temp_outputpath);
			if (!file.exists()) {
				file.createNewFile();
			}else{
				file.deleteOnExit();
				file.createNewFile();
			}
			FileOutputStream localFileOutputStream = new FileOutputStream(temp_outputpath);
			localFileOutputStream.write(newdex);
			localFileOutputStream.flush();
			localFileOutputStream.close();
			File filetest = new File(temp_outputpath);
			System.out.println("-----------------------���ϲ��ӿǺ󳤶ȣ�"+filetest.length());
			System.out.println("------------���ӿ���ɣ�");
		}catch(Exception e){
			System.out.println("------------���ӿ��쳣��");
			e.printStackTrace();
			return;
		}
	}
	
	
	//�ӿ����
	public static void pre_main(){
		try {
			File payloadSrcFile = new File("force/ForceApkObj.apk");   //��Ҫ�ӿǵĳ���
			System.out.println("��Ҫ�ӿ�apk size:"+payloadSrcFile.length());
			File unShellDexFile = new File("force/ForceApkObj.dex");	//���dex
			System.out.println("�ѿ�dex size:"+unShellDexFile.length());
			byte[] payloadArray = encrpt(readFileBytes(payloadSrcFile));//�Զ�������ʽ����apk�������м��ܴ���//��ԴApk���м��ܲ���
			byte[] unShellDexArray = readFileBytes(unShellDexFile);//�Զ�������ʽ����dex
			int payloadLen = payloadArray.length;
			int unShellDexLen = unShellDexArray.length;
			int totalLen = payloadLen + unShellDexLen +4;//���4�ֽ��Ǵ�ų��ȵġ�
			byte[] newdex = new byte[totalLen]; // �������µĳ���
			//��ӽ�Ǵ���    src  startpos  targetobject targetstartpos length
			System.arraycopy(unShellDexArray, 0, newdex, 0, unShellDexLen);//�ȿ���dex����
			//��Ӽ��ܺ��apk����
			System.arraycopy(payloadArray, 0, newdex, unShellDexLen, payloadLen);//����dex���ݺ��濽��apk������
			//��Ӽ���apk���ݳ���
			System.arraycopy(intToByte(payloadLen), 0, newdex, totalLen-4, 4);//���4Ϊ����
            //�޸ĺϲ�֮��DEX file size�ļ�ͷ
			fixFileSizeHeader(newdex);
			//�޸ĺϲ�֮��DEX SHA1 �ļ�ͷ
			fixSHA1Header(newdex);
			//�޸ĺϲ�֮��DEX CheckSum�ļ�ͷ
			fixCheckSumHeader(newdex);
			String str = "force/classes.dex";
			File file = new File(str);
			if (!file.exists()) {
				file.createNewFile();
			}
			FileOutputStream localFileOutputStream = new FileOutputStream(str);
			localFileOutputStream.write(newdex);
			localFileOutputStream.flush();
			localFileOutputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	//ֱ�ӷ������ݣ����߿�������Լ����ܷ���
	private static byte[] encrpt(byte[] srcdata){
		for(int i = 0;i<srcdata.length;i++){
			srcdata[i] = (byte)(0xFF ^ srcdata[i]);
		}
		return srcdata;
	}

	/**
	 * �޸�dexͷ��CheckSum У����
	 * @param dexBytes
	 */
	private static void fixCheckSumHeader(byte[] dexBytes) {
		Adler32 adler = new Adler32();
		adler.update(dexBytes, 12, dexBytes.length - 12);//��12���ļ�ĩβ����У����
		long value = adler.getValue();
		int va = (int) value;
		byte[] newcs = intToByte(va);
		//��λ��ǰ����λ��ǰ������
		byte[] recs = new byte[4];
		for (int i = 0; i < 4; i++) {
			recs[i] = newcs[newcs.length - 1 - i];
			System.out.println(Integer.toHexString(newcs[i]));
		}
		System.arraycopy(recs, 0, dexBytes, 8, 4);//Ч���븳ֵ��8-11��
		System.out.println("CheckSum:"+Long.toHexString(value));
		System.out.println();
	}


	/**
	 * int תbyte[]
	 * @param number
	 * @return
	 */
	public static byte[] intToByte(int number) {
		byte[] b = new byte[4];
		for (int i = 3; i >= 0; i--) {
			b[i] = (byte) (number % 256);
			number >>= 8;
		}
		return b;
	}

	/**
	 * �޸�dexͷ sha1ֵ
	 * @param dexBytes
	 * @throws NoSuchAlgorithmException
	 */
	private static void fixSHA1Header(byte[] dexBytes)
			throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("SHA-1");
		md.update(dexBytes, 32, dexBytes.length - 32);//��32Ϊ����������sha--1
		byte[] newdt = md.digest();
		System.arraycopy(newdt, 0, dexBytes, 12, 20);//�޸�sha-1ֵ��12-31��
		//���sha-1ֵ�����п���
		String hexstr = "";
		for (int i = 0; i < newdt.length; i++) {
			hexstr += Integer.toString((newdt[i] & 0xff) + 0x100, 16)
					.substring(1);
		}
		System.out.println("SHA1:"+hexstr);
	}

	/**
	 * �޸�dexͷ file_sizeֵ
	 * @param dexBytes
	 */
	private static void fixFileSizeHeader(byte[] dexBytes) {
		//���ļ�����
		byte[] newfs = intToByte(dexBytes.length);
		System.out.println(Integer.toHexString(dexBytes.length));
		byte[] refs = new byte[4];
		//��λ��ǰ����λ��ǰ������
		for (int i = 0; i < 4; i++) {
			refs[i] = newfs[newfs.length - 1 - i];
			System.out.println(Integer.toHexString(newfs[i]));
		}
		System.arraycopy(refs, 0, dexBytes, 32, 4);//�޸ģ�32-35��
	}


	/**
	 * �Զ����ƶ����ļ�����
	 * @param file
	 * @return
	 * @throws IOException
	 */
	private static byte[] readFileBytes(File file) throws IOException {
		byte[] arrayOfByte = new byte[1024];
		ByteArrayOutputStream localByteArrayOutputStream = new ByteArrayOutputStream();
		FileInputStream fis = new FileInputStream(file);
		while (true) {
			int i = fis.read(arrayOfByte);
			if (i != -1) {
				localByteArrayOutputStream.write(arrayOfByte, 0, i);
			} else {
				return localByteArrayOutputStream.toByteArray();
			}
		}
	}
}
