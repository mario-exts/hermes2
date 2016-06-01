package com.gaia.hermes2.http.deserializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Enumeration;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;

import org.apache.commons.io.IOUtils;

import com.gaia.hermes2.statics.F;
import com.mario.entity.message.MessageRW;
import com.mario.entity.message.transcoder.http.HttpMessageDeserializer;
import com.mario.exception.InvalidDataFormatException;
import com.nhb.common.data.PuObject;
import com.nhb.common.data.PuValue;

public class Hermes2HttpGatewayDeserialier extends HttpMessageDeserializer {

	public Hermes2HttpGatewayDeserialier() {
		super();
		getLogger().debug("init deserializer");
	}

	@Override
	protected void decodeHttpRequest(ServletRequest data, MessageRW message) {
		HttpServletRequest request = (HttpServletRequest) data;
		if (request != null) {
			PuObject params = new PuObject();
			Enumeration<String> it = request.getParameterNames();
			while (it.hasMoreElements()) {
				String key = it.nextElement();
				String value = request.getParameter(key);
				params.set(key, value);
			}
			if (request.getMethod().equalsIgnoreCase("post")) {
				if (request.getContentType().toLowerCase().contains("multipart/form-data")) {
					getLogger().debug("Posted data is in multipart format");
					try {
						Collection<Part> parts = request.getParts();
						for (Part part : parts) {
							if (part.getSize() > 0) {
								byte[] bytes = new byte[(int) part.getSize()];
								part.getInputStream().read(bytes, 0, bytes.length);
								params.setRaw(part.getName(), bytes);
							}
						}
					} catch (Exception e) {
						getLogger().error("Error while get data from request", e);
						throw new InvalidDataFormatException("Error while get data from request: " + e.getMessage(), e);
					}
				} else {
					getLogger().debug("Posted data in raw format, trying to parse as json");
					StringWriter sw = new StringWriter();
					InputStream is = null;
					try {
						is = request.getInputStream();
						IOUtils.copy(is, sw);
						params.addAll(PuObject.fromJSON(sw.toString()));
					} catch (IOException ex) {
						throw new RuntimeException(ex);
					} catch (Exception e) {
						if (is != null) {
							params.set(F.BODY, new PuValue(sw.toString()));
						}
					} finally {
						try {
							sw.close();
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
						if (is != null) {
							try {
								is.close();
							} catch (IOException e) {
								throw new RuntimeException(e);
							}
						}
					}
				}
			}
			message.setData(params);
		} else {
			throw new NullPointerException("Cannot parse null request");
		}
	}

}
