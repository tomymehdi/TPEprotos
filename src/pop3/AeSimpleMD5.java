package pop3;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class AeSimpleMD5 { 
	
	public static void main(String[] args) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		String s = "<1896.697170952@dbc.mtview.ca.us>tanstaaf";
		
		String resp = AeSimpleMD5.MD5(s);
		
		//System.out.println(new String(AeSimpleMD5.convertToHex(resp.getBytes())));
		
		
	}
	 
    private static String convertToHex(byte[] data) { 
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < data.length; i++) { 
            int halfbyte = (data[i] >>> 4) & 0x0F;
            int two_halfs = 0;
            do { 
                if ((0 <= halfbyte) && (halfbyte <= 9)) 
                    buf.append((char) ('0' + halfbyte));
                else 
                    buf.append((char) ('a' + (halfbyte - 10)));
                halfbyte = data[i] & 0x0F;
            } while(two_halfs++ < 1);
        } 
        return buf.toString();
    } 
 
    public static String MD5(String text) throws NoSuchAlgorithmException, UnsupportedEncodingException  { 
        MessageDigest md;
        md = MessageDigest.getInstance("MD5");
        byte[] md5hash = new byte[32];
        md.update(text.getBytes("iso-8859-1"), 0, text.length());
        md5hash = md.digest();
        System.out.println( convertToHex(md5hash) );
        System.out.println(md.getProvider());
        return null;
    } 
}