package com.bcol.vtd.api.preaprobadoca.web.rest;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.List;



@Provider
@Produces(MediaType.APPLICATION_JSON)
public class JsonProvider implements MessageBodyWriter<Object>, ExceptionMapper<JsonProcessingException> {

	private static final Logger logger = LogManager.getLogger();
	
	@Override
	public long getSize(Object object, Class<?> type, Type genericType,
			Annotation[] annotations, MediaType mediaType) {
		return -1;
	}

	@Override
	public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations,
			MediaType mediaType) {
		
		boolean isWritable;
		
		if (List.class.isAssignableFrom(type) && genericType instanceof ParameterizedType) {
			
			ParameterizedType parameterizedType = (ParameterizedType) genericType;
			Type[] actualTypeArgs = parameterizedType.getActualTypeArguments();
			isWritable = (actualTypeArgs.length == 1);
		} else {
			isWritable = true;
		}
		
		return isWritable;
	}

	@Override
	public void writeTo(Object object, Class<?> type, Type genericType,
			Annotation[] annotations, MediaType mediaType,
			MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
			throws IOException {

		//Jackson mapper
		ObjectMapper mapper = new ObjectMapper();
		mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd"));
		mapper.writeValue(entityStream, object);
		
	}
	
	 @Override
	  public Response toResponse(final JsonProcessingException jpe) {
	        // Create and return an appropriate response here
			logger.error(EnumLogVentasDigitales.ERROR_API_VENTAS_DIGITALES_510.getValue() + jpe.getMessage());
			return Response.status(Status.BAD_REQUEST)
					.entity(EnumLogVentasDigitales.ERROR_API_VENTAS_DIGITALES_510.getValue()+CodigosRespuestaServicios.SEG_005.getDescripcion())
					.build();
	    }

}
