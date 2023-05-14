package com.sense.sdk.saml;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.Deflater;

import org.apache.commons.codec.binary.Base64;

public class SAMLRequestFactory {

	public String getSAMLRequest(String idpSSOUrl, String acsUrl, String spEntityId, String nameId, String timeZone) {
		SAMLRequest samlRequest = new SAMLRequest();
		String str = "";
		String samlRequestXmlString = "";
		try {
			samlRequestXmlString = samlRequest.createRequestXmlString(idpSSOUrl, acsUrl, spEntityId, nameId, timeZone);
			byte[] requestByte = samlRequestXmlString.getBytes("utf-8");

			requestByte = compress(requestByte);

			str = new Base64().encodeAsString(requestByte);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return str;
	}

	private static byte[] compress(byte[] inputByte) throws IOException {
		int len = 0;
		Deflater defl = new Deflater(Deflater.DEFAULT_COMPRESSION, true);
		defl.setInput(inputByte);
		defl.finish();
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		byte[] outputByte = new byte[1024];
		try {
			while (!defl.finished()) {
				// 压缩并将压缩后的内容输出到字节输出流bos中
				len = defl.deflate(outputByte);
				bos.write(outputByte, 0, len);
			}
			defl.end();
		} finally {
			bos.close();
		}
		return bos.toByteArray();
	}
}
