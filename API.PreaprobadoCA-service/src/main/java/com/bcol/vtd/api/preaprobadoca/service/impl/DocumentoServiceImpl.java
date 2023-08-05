package com.bcol.vtd.api.preaprobadoca.service.impl;


import com.bcol.vtd.api.preaprobadoca.service.DocumentoService;
import com.bcol.vtd.api.preaprobadoca.util.PlantillasUtil;
import com.bcol.vtd.api.preaprobadoca.util.VentaDigitalUtil;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DocumentoServiceImpl implements DocumentoService {
	
	private static final Logger logger = LogManager.getLogger(DocumentoServiceImpl.class);

	VentaDigitalUtil ventaDigitalUtil;
	PersistenciaService persistenciaService;
	InformacionUsuarioService infousuario;
	ParameterService parameterService;

	@Inject
	public DocumentoServiceImpl(VentaDigitalUtil ventaDigitalUtil, PersistenciaService persistenciaService,
								InformacionUsuarioService infousuario, ParameterService parameterService){
		this.ventaDigitalUtil = ventaDigitalUtil;
		this.persistenciaService = persistenciaService;
		this.infousuario = infousuario;
		this.parameterService = parameterService;
	}

	public Response obtenerCartaBienvenida(HttpServletRequest requestContext, VentaDigitalCrediAgil ventaDigitalCrediAgil, String idSesion,
										   String tokenApp) {
		
		VentaDigital infoVisitaVD = null;
		RespuestaServicio respuestaServicio=new RespuestaServicio();
		InformacionTransaccion informacionTransaccion;
		VentaDigital datosCLiente;
		ExcepcionServicio excepcionServicio = null;
		ExcepcionServicio excepcionManejoConnectorError = null;

		File archivoFinal = null;
		String fileExtention = "pdf";
		String codeExcepcionDescarga = null;
		int numeroIntentosDescarga=0;
		ResponseBuilder response;
		String ipcliente = null;
		String canal = null;

		try {
		
			if(ventaDigitalUtil.validarIntentosDescarga(idSesion, ConstantesCA.CONSTANTE_DESCARGA_DOCUMENTO, numeroIntentosDescarga)) {

				infoVisitaVD=infousuario.getconsultarInformacionVisita(idSesion);
				

				List<Parametro> datosCartaBienvenida = obtenerDatosDescargaDocumento(ventaDigitalCrediAgil, infoVisitaVD);

				if(datosCartaBienvenida != null) {

					File cartaBienvenida = getCartaBienvenida(datosCartaBienvenida);

					String documento = "";
					documento = ventaDigitalCrediAgil.getSolicitud().getOfertaDigital().getDocumentos().get(0)
							.getDocumento();

					List<File> listFiles = new ArrayList<File>();
					listFiles.add(cartaBienvenida);

					listFiles.add(ventaDigitalUtil.transformStringB64ToFile(documento, fileExtention));

					archivoFinal = ventaDigitalUtil.mergePDFFiles(listFiles, fileExtention);
					byte[] fileContent = FileUtils.readFileToByteArray(archivoFinal);


					String base64Document = java.util.Base64.getEncoder().encodeToString(fileContent);

					ResponseService res = new ResponseService();
					res.setStatus(StatusResponse.SUCCESS.getName());
					res.setOutput(base64Document);
					// TODO: definir el nombre a devolver

					return Response.ok(res).build();
				} else{
					try {
						codeExcepcionDescarga = ConstantesCA.errorMaximoDescargaDocs;
						respuestaServicio = ventaDigitalUtil.obtenerErrorApi(ventaDigitalCrediAgil.getProducto().getCodigoProducto(),ventaDigitalCrediAgil, codeExcepcionDescarga, idSesion, ConstantesCA.CONSTANTE_DESCARGA_DOCUMENTO);
						ventaDigitalCrediAgil = (VentaDigitalCrediAgil) HashUtil.desEncriptarInformacionSensible(ventaDigitalCrediAgil);
						persistenciaService.persistirVentasDigitales(null, respuestaServicio, ventaDigitalCrediAgil, false,
								Constantes.pagina3bFPCA, Constantes.ESTADO_SOLICITUD_EN_PROCESO_ENTREGA, true);
					} catch (ConectorClientException e) {
						excepcionServicio = new ExcepcionServicio(CodigosRespuestaCA.ERROR_INTERNO_DESCARGA_DOCUMENTOS.getCodigo(), CodigosRespuestaCA.ERROR_INTERNO_DESCARGA_DOCUMENTOS.getDescripcion());
						ventaDigitalCrediAgil = (VentaDigitalCrediAgil) HashUtil.desEncriptarInformacionSensible(ventaDigitalCrediAgil);
						persistenciaService.persistirVentasDigitales(excepcionServicio, respuestaServicio, ventaDigitalCrediAgil, false,
								Constantes.pagina3bFPCA, Constantes.ESTADO_SOLICITUD_EN_PROCESO_ENTREGA, true);
						HashMap<String, Object> mapResponseError = (HashMap<String, Object>) ventaDigitalUtil.manejarConnectorApiErrores(e, ConstantesCA.CONSTANTE_DESCARGA_DOCUMENTO);
						excepcionManejoConnectorError = mapResponseError.get(ConstantesCA.EXCEPCION_ERROR_API_ERRORES) instanceof ExcepcionServicio ? (ExcepcionServicio) mapResponseError.get(ConstantesCA.EXCEPCION_ERROR_API_ERRORES) : null;
						persistenciaService.persistirVentasDigitales(excepcionManejoConnectorError, respuestaServicio, ventaDigitalCrediAgil, false,
								Constantes.pagina3bFPCA, Constantes.ESTADO_SOLICITUD_EN_PROCESO_ENTREGA,true);
						respuestaServicio = new RespuestaServicio(CodigosRespuestaCA.ERROR_INTERNO_DESCARGA_DOCUMENTOS.getCodigo(), CodigosRespuestaCA.ERROR_INTERNO_DESCARGA_DOCUMENTOS.getDescripcion());

					}
				}
			} else {
				try {
					codeExcepcionDescarga = ConstantesCA.errorMaximoDescargaDocs;
					respuestaServicio = ventaDigitalUtil.obtenerErrorApi(ventaDigitalCrediAgil.getProducto().getCodigoProducto(),ventaDigitalCrediAgil, codeExcepcionDescarga, idSesion, ConstantesCA.CONSTANTE_DESCARGA_DOCUMENTO);
					ventaDigitalCrediAgil = (VentaDigitalCrediAgil) HashUtil.desEncriptarInformacionSensible(ventaDigitalCrediAgil);
					persistenciaService.persistirVentasDigitales(null, respuestaServicio, ventaDigitalCrediAgil, false,
							Constantes.pagina3bFPCA, Constantes.ESTADO_SOLICITUD_EN_PROCESO_ENTREGA, true);
				} catch (ConectorClientException e) {
					excepcionServicio = new ExcepcionServicio(CodigosRespuestaCA.ERROR_DESCARGA_DOCUMENTOS.getCodigo(), CodigosRespuestaCA.ERROR_DESCARGA_DOCUMENTOS.getDescripcion());
					ventaDigitalCrediAgil = (VentaDigitalCrediAgil) HashUtil.desEncriptarInformacionSensible(ventaDigitalCrediAgil);

					persistenciaService.persistirVentasDigitales(excepcionServicio, respuestaServicio, ventaDigitalCrediAgil, false,
							Constantes.pagina3bFPCA, Constantes.ESTADO_SOLICITUD_EN_PROCESO_ENTREGA, true);
					HashMap<String, Object> mapResponseError = (HashMap<String, Object>) ventaDigitalUtil.manejarConnectorApiErrores(e, ConstantesCA.CONSTANTE_DESCARGA_DOCUMENTO);
					excepcionManejoConnectorError = mapResponseError.get(ConstantesCA.EXCEPCION_ERROR_API_ERRORES) instanceof ExcepcionServicio ? (ExcepcionServicio) mapResponseError.get(ConstantesCA.EXCEPCION_ERROR_API_ERRORES) : null;
					persistenciaService.persistirVentasDigitales(excepcionManejoConnectorError, respuestaServicio, ventaDigitalCrediAgil, false,
							Constantes.pagina3bFPCA, Constantes.ESTADO_SOLICITUD_EN_PROCESO_ENTREGA, true);
					respuestaServicio = new RespuestaServicio(CodigosRespuestaCA.ERROR_DESCARGA_DOCUMENTOS.getCodigo(), CodigosRespuestaCA.ERROR_DESCARGA_DOCUMENTOS.getDescripcion());

				}

			}

			ventaDigitalUtil.addCodigoProducto(respuestaServicio,
					ventaDigitalCrediAgil.getProducto().getCodigoProducto());
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(respuestaServicio).build();
				
			
		} catch (Exception e) {

			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(respuestaServicio).build();
		}
	}

	public File getCartaBienvenida(List<Parametro> datosCartaBienvenida){

		File resultFile = null;
		RespuestaServicioEnvioCorreo respuestaDocumento =  getDocumento(datosCartaBienvenida);

		if(respuestaDocumento.getEstadoRespuesta()) {
			resultFile = (File) respuestaDocumento.getObjetoRespuesta();
		}

		return resultFile;
	}
	
	public RespuestaServicioEnvioCorreo getDocumento(List<Parametro> listaParametros) {
		
		RespuestaServicioEnvioCorreo respuestaServicioEnvioCorreo = null;

		PlantillasUtil plantillasUtil = new PlantillasUtil(listaParametros,parameterService);

		plantillasUtil.createFile();

		respuestaServicioEnvioCorreo =  plantillasUtil.getRespuestaServicioEnvioCorreo();

		if (respuestaServicioEnvioCorreo.getEstadoRespuesta()){

			respuestaServicioEnvioCorreo.setObjetoRespuesta(plantillasUtil.getFilePdf());

		}

		return respuestaServicioEnvioCorreo;
	}

	public List<Parametro>  obtenerDatosDescargaDocumento(VentaDigitalCrediAgil ventaDigitalCrediAgil,
														  VentaDigital infoVisitaVD) {

		DecimalFormat formatNum = new DecimalFormat("#,###");

		List<Parametro> listaParametros= new ArrayList<Parametro>();
		listaParametros.add(new Parametro(ConstantesEnviadorCorreo.EMAIL.getValue(), ""));

		return listaParametros;
	}
	
}
