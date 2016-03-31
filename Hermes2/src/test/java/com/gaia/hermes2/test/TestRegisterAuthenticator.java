package com.gaia.hermes2.test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import com.nhb.common.encrypt.sha.SHAEncryptor;

public class TestRegisterAuthenticator {

	public static void main(String[] args) throws FileNotFoundException, IOException {
		String path = "/Users/bachden/Desktop/TestAPNS/TestAPNS_Certificates.p12";
		try (InputStream is = new FileInputStream(path)) {
			byte[] bytes = new byte[is.available()];
			is.read(bytes);

			System.out.println("checksum: " + SHAEncryptor.sha512Hex(bytes));
		}
	}
}
