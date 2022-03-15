package com.wayapaychat.paymentgateway;

import com.wayapaychat.paymentgateway.utils.Crypto;

public class Test {
	
	public static void main(String a[]) throws Exception {
		/*
		* Secret Key must be in the form of 16 byte like,
		*
		* private static final byte[] secretKey = new byte[] { ‘m’, ‘u’, ‘s’, ‘t’, ‘b’,
		* ‘e’, ‘1’, ‘6’, ‘b’, ‘y’, ‘t’,’e’, ‘s’, ‘k’, ‘e’, ‘y’};
		*
		* below is the direct 16byte string we can use
		*/
		String secretKey = "wayatest1726chat";
		String encodedBase64Key = Crypto.encodeKey(secretKey);
		System.out.println("EncodedBase64Key = " + encodedBase64Key); // This need to be share between client and server
		// To check actual key from encoded base 64 secretKey
		// String toDecodeBase64Key = decodeKey(encodedBase64Key);
		// System.out.println(“toDecodeBase64Key = “+toDecodeBase64Key);
		String toEncrypt = "{\"tranId\":\"S65097\", \"name\":\"EMMANUEL NJOKU\"}";
		System.out.println("Plain text = " + toEncrypt);
		// AES Encryption based on above secretKey
		String encrStr = Crypto.encrypt(toEncrypt, encodedBase64Key);
		System.out.println("Cipher Text: Encryption of str = " + encrStr);
		//Encode the Encryption
		String encodedBase64Encrypt = Crypto.encodeKey(encrStr);
		System.out.println("Encode = " + encodedBase64Encrypt);
		//Decode the encode base 64
		String decodedBase64Encrypt = Crypto.decodeKey(encodedBase64Encrypt);
		System.out.println("Decode = " + decodedBase64Encrypt);
		// AES Decryption based on above secretKey
		String decrStr = Crypto.decrypt(decodedBase64Encrypt, encodedBase64Key);
		System.out.println("Decryption of str = " + decrStr);
	}

}
