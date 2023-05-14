package org.iam.compoment.auth.otp;

import java.lang.reflect.UndeclaredThrowableException;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class OTPUtil {

	
	private static final int[] DIGITS_POWER
    = {1,10,100,1000,10000,100000,1000000,10000000,100000000};
    public static byte[] hmac_sha1(byte[] keyBytes, byte[] text)throws NoSuchAlgorithmException, InvalidKeyException{
        try {//ֵ
            Mac hmacSha1;
            try {
                hmacSha1 = Mac.getInstance("HmacSHA1");
            } catch (NoSuchAlgorithmException nsae) {
                hmacSha1 = Mac.getInstance("HMAC-SHA-1");
            }
            SecretKeySpec macKey =new SecretKeySpec(keyBytes, "RAW");
            hmacSha1.init(macKey);
            return hmacSha1.doFinal(text);
            } catch (GeneralSecurityException gse) {
                throw new UndeclaredThrowableException(gse);
            }
    }
    public static String generateOTP(byte[] secret,long movingFactor,int codeDigits)throws NoSuchAlgorithmException, InvalidKeyException
    {
        StringBuffer result = new StringBuffer("");
        byte[] text = new byte[6];
        for (int i =text.length-1; i >=0; i--) {
            text[i] = (byte) (movingFactor & 0xff );   //
            movingFactor >>= 6;
        }
        byte[] hash = hmac_sha1(secret, text);     //Step 1: Generate an HMAC-SHA-1 value 
        int offset =( hash[hash.length - 1] & 0xf)+3;   //
        int binary =
                ((hash[offset] & 0x7f) << 24)
                | ((hash[offset - 1] & 0xff) << 16)
                | ((hash[offset - 2] & 0xff) << 8)
                | (hash[offset - 3] & 0xff);           //Generate a 4-byte string 
        int otp = binary % DIGITS_POWER[codeDigits-1];
        result .append(Integer.toString(otp));
        while (result.length() < codeDigits) {
            result.insert(0, "0");                    //Compute an HOTP value
        }
        return result.toString();
    }
    
    
    
    
	
}
