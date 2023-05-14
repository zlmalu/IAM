package com.sense.iam.sso.ltpa;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

import cn.hutool.core.codec.Base64;

 /** 
     * Lightweight Third Party Authentication. Generates and validates ltpa tokens used in Domino single sign on 
     * environments. Does not work with WebSphere SSO tokens. You need a properties file named LtpaToken.properties which 
     * holds two properties. 
     * 
     * <pre> 
     * ) domino.secret=The base64 encoded secret found in the field LTPA_DominoSecret in the SSO configuration document. 
     * ) cookie.domain=The domain you want generated cookies to be from. e.g. '.domain.com' (Note the leading dot) 
     *</pre> 
     * 
     * @author $Author: rkelly $ 
     * @version $Revision: 1.1 $ 
     * @created $Date: 2003/04/07 18:22:14 $ 
     */ 
public final class LtpaToken { 
        private byte[] creation; 
        private Date creationDate; 
        private byte[] digest; 
        private byte[] expires; 
        private Date expiresDate; 
        private byte[] hash; 
        private byte[] header; 
        private String ltpaToken; 
        private Properties properties = null; 
        private byte[] rawToken; 
        private byte[] user; 
      
        
      

            /** 
         * Constructor for the LtpaToken object 
         * 
         * @param token 
         *            Description of the Parameter 
         */ 
        public LtpaToken(String token) { 
            init(); 
            ltpaToken = token; 
            rawToken = Base64.decode(token); 
            user = new byte[(rawToken.length) - 40]; 
            for (int i = 0; i < 4; i++) { 
                header[i] = rawToken[i]; 
            } 
            for (int i = 4; i < 12; i++) { 
                creation[i - 4] = rawToken[i]; 
            } 
            for (int i = 12; i < 20; i++) { 
                expires[i - 12] = rawToken[i]; 
            } 
            for (int i = 20; i < (rawToken.length - 20); i++) { 
                user[i - 20] = rawToken[i]; 
            } 
            for (int i = (rawToken.length - 20); i < rawToken.length; i++) { 
                digest[i - (rawToken.length - 20)] = rawToken[i]; 
            } 
            creationDate = new Date(Long.parseLong(new String(creation), 16) * 1000); 
            expiresDate = new Date(Long.parseLong(new String(expires), 16) * 1000); 
        } 

        /** 
         * Constructor for the LtpaToken object 
         */ 
        private LtpaToken() { 
            init(); 
        } 



        /** 
         * Gets the creationDate attribute of the LtpaToken object 
         * 
         * @return The creationDate value 
         */ 
        public Date getCreationDate() { 
            return creationDate; 
        } 

        /** 
         * Gets the expiresDate attribute of the LtpaToken object 
         * 
         * @return The expiresDate value 
         */ 
        public Date getExpiresDate() { 
            return expiresDate; 
        } 

        /** 
         * Gets the user attribute of the LtpaToken object 
         * 
         * @return The user value 
         */ 
        public String getCanonicalUser() { 
            return new String(user); 
        } 
        
        public String getUser(String prefix, String suffix) {
        	String userName = new String(user);
        	if (prefix !=null && !prefix.equals("")) {
    			userName = userName.substring(userName.indexOf(prefix) + prefix.length());
    		}
    		if (suffix ==null || suffix.equals("")) {
    			suffix = "/";
    		} 
        	return userName.substring(0, userName.indexOf("/"));
        }
        
        public String getUser() { 
            return new String(user); 
        } 


        /** 
         * Validates the SHA-1 digest of the token with the Domino secret key. 
         * 
         * @return Returns true if valid. 
         */ 
        public  boolean isValid() { 
            boolean validDigest = false; 
            boolean validDateRange = false; 
            byte[] newDigest; 
            byte[] bytes = null; 
            Date now = new Date(); 

            MessageDigest md = getDigest(); 

            bytes = concatenate(bytes, header); 

            bytes = concatenate(bytes, creation); 

            bytes = concatenate(bytes, expires); 

            bytes = concatenate(bytes, user); 
            String secret="/Jq/2bPWTMF6EGvcCB4UgggRUfktaQHBOYAPBmhVSc/Tf16yWnEvZE8BPwxq9iDTiTYJ/pIelGsb2VGY9XPg4rgmi0JRHFUpvTD77XDQZlxlZza+ZBirMiZTVZUzLvsv7R4PijrzY2ROVi/9EmZ/nxGP8jR3DNL5YzdgCpUJFDbz4d0anHfk1pHZ4lygKCWZhHdXmiYBMTk7o7hXejqiS/NT0tb5ObnZBziQTiPW5ZngGogv3G4dd/ils6MdZaN1YR0xk4vFHLUxOfad/LWfuMlkuORZVG6MKHArA6BDsiXOInEkBDGDIi25N1xH92DayTDBfWziR2tHskCtmFPFojy2clsiHXo65a3Gae0vXUQ=";
            bytes = concatenate(bytes, Base64.decode(secret)); 

            newDigest = md.digest(bytes); 

            validDigest = MessageDigest.isEqual(digest, newDigest); 
            
            validDateRange = now.after(creationDate) && now.before(expiresDate); 

            return validDigest & validDateRange; 
        } 

        /** 
         * String representation of LtpaToken object. 
         * 
         * @return Returns token String suitable for cookie value. 
         */ 
        public String toString() { 
            return ltpaToken; 
        } 

        /** 
         * Creates a new SHA-1 <code>MessageDigest</code> instance. 
         * 
         * @return The instance. 
         */ 
        private MessageDigest getDigest() { 
            try { 
                return MessageDigest.getInstance("SHA-1");            
            } catch (NoSuchAlgorithmException nsae) { 
                nsae.printStackTrace(); 
            } 
            return null; 
        } 

        /** 
         * Description of the Method 
         */ 
        private void init() { 

            creation = new byte[8]; 
            digest = new byte[20]; 
            expires = new byte[8]; 
            hash = new byte[20]; 
            header = new byte[4]; 

        } 

        /** 
         * Validates the SHA-1 digest of the token with the Domino secret key. 
         * 
         * @param ltpaToken 
         *            Description of the Parameter 
         * @return The valid value 
         */ 
        public static boolean isValid(String ltpaToken) { 
            LtpaToken ltpa = new LtpaToken(ltpaToken); 
            return ltpa.isValid(); 
        } 

        /** 
         * Generates a new LtpaToken with given parameters. 
         * 
         * @param canonicalUser 
         *            User name in canonical form. e.g. 'CN=Robert Kelly/OU=MIS/O=EBIMED'. 
         * @param tokenCreation 
         *            Token creation date. 
         * @param tokenExpires 
         *            Token expiration date. 
         * @return The generated token. 
         */ 
        public static LtpaToken generate(String canonicalUser, Date tokenCreation, Date tokenExpires,String  secret) { 
            LtpaToken ltpa = new LtpaToken(); 
            System.out.println("Generating token for " + canonicalUser); 
            Calendar calendar = Calendar.getInstance(); 
            MessageDigest md = ltpa.getDigest(); 
            ltpa.header = new byte[] { 0, 1, 2, 3 }; 
            byte[] token = null; 
            calendar.setTime(tokenCreation); 
            ltpa.creation = Long.toHexString(calendar.getTimeInMillis() / 1000).toUpperCase().getBytes(); 
            calendar.setTime(tokenExpires); 
            ltpa.expires = Long.toHexString(calendar.getTimeInMillis() / 1000).toUpperCase().getBytes();  
            //try {
			//	canonicalUser = new String(canonicalUser.getBytes(), "GB2312");
			//} catch (UnsupportedEncodingException e) {
				// TODO 自己主动生成 catch 块
			//	e.printStackTrace();
			//}
            ltpa.user = canonicalUser.getBytes(); 
            //ltpa.user = canonicalUser.getBytes(Charset.forName("GB18030")); 

            token = concatenate(token, ltpa.header); 
            token = concatenate(token, ltpa.creation); 
            token = concatenate(token, ltpa.expires); 
            token = concatenate(token, ltpa.user); 
            md.update(token); 
            ltpa.digest = md.digest(Base64.decode(secret)); 
            token = concatenate(token, ltpa.digest); 
            LtpaToken ltpaTokenValue=new LtpaToken(new String(Base64.encode(token)));
            return ltpaTokenValue;            
        } 

        /** 
         * Helper method to concatenate a byte array. 
         * 
         * @param a 
         *            Byte array a. 
         * @param b 
         *            Byte array b. 
         * @return a + b. 
         */ 
        private static byte[] concatenate(byte[] a, byte[] b) { 
            if (a == null) { 
                return b; 
            } else { 
                byte[] bytes = new byte[a.length + b.length]; 

                System.arraycopy(a, 0, bytes, 0, a.length); 
                System.arraycopy(b, 0, bytes, a.length, b.length); 
                return bytes; 
            } 
        } 

        public String getLtpaToken() { 
            return ltpaToken; 
        } 

        public void setLtpaToken(String ltpaToken) { 
            this.ltpaToken = ltpaToken; 
        } 
    }