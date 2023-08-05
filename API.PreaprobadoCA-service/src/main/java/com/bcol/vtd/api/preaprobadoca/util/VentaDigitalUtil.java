package com.bcol.vtd.api.preaprobadoca.util;

import com.bcol.vtd.api.preaprobadoca.exception.CrediAgilException;
import com.bcol.vtd.api.preaprobadoca.factory.ProxyFactory;
import com.bcol.vtd.api.preaprobadoca.security.ISecurity;
import com.bcol.vtd.api.preaprobadoca.service.connector.CrediagilConnector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;

import javax.inject.Inject;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.*;

public class VentaDigitalUtil {

    private static final Logger logger = LogManager.getLogger(VentaDigitalUtil.class);

    @Inject
    private CrediagilConnector connector;

    @Inject
    ParameterService parameterService;

    @Inject
    PersistenciaService persistenciaService;

    @Inject
    private ISecurity security;
    
	@Inject
	InformacionUsuarioService informacionUsuarioService;
	
    @Inject
    private ProxyFactory proxyFactory;

    public static final double MESES_YEAR = 12D;
    
    public InformacionTransaccion obtenerInformacionTransaccion(String ipcliente, String idSesion, String tokenApp, String canal,
                                                                String pasoFuncional) throws CrediAgilException {
    	
    	if(!validateIpRegex(ipcliente)) {
			throw new CrediAgilException(CodigosRespuestaServicios.SRV_001.getCodigo());
		}

        InformacionTransaccion informacionTransaccion = new InformacionTransaccion();
        informacionTransaccion.setIpCliente(ipcliente);
        informacionTransaccion.setIdSesion(idSesion);
        informacionTransaccion.setTokenApp(tokenApp);
        informacionTransaccion.setPasoFuncional(pasoFuncional);
        informacionTransaccion.setIdAplicacion(Constantes.ID_APLICACION_PREAPROBADO_CREDIAGIL);
        informacionTransaccion.setCanal(canal);
        informacionTransaccion.setFechaHoraTransaccion(new Date());

        return informacionTransaccion;

    }


    public static Map<String, String> map(List<String> listParameters, Map<String, String> parameters) {
        Map<String, String> map = new HashMap<>();
        for (String key : listParameters) {
            map.put(key, parameters.get(key));
        }
        return map;
    }


   public File transformStringB64ToFile(String encodeStringB64, String fileExtention) throws IOException {

        byte[] byteArrayFile = Base64.getDecoder().decode(encodeStringB64);

        File tempFile = null;
        Path file=null;

        try {
        	
        	FileAttribute<Set<PosixFilePermission>> attributes
		      = PosixFilePermissions.asFileAttribute(new HashSet<>(
		          Arrays.asList(PosixFilePermission.OWNER_WRITE,
		                        PosixFilePermission.OWNER_READ)));
            file = Files.createTempFile("tmp", ".".concat(fileExtention), attributes);
            tempFile=file.toFile();
        } catch (IOException e) {
            logger.info(VentaDigitalUtil.class +
                    "transformStringB64ToFile: " + e.getMessage(), e);
        }
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(tempFile);
        } catch (FileNotFoundException e) {
            logger.info(VentaDigitalUtil.class +
                    "Error iniciando FileOutputStream: " + e.getMessage(), e);
        }
        try {
            fos.write(byteArrayFile);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            logger.info(VentaDigitalUtil.class +
                    "Error escribiendo en el archivo: " + e.getMessage(), e);
        } finally {
            if (fos != null) {
                fos.close();
            }
        }
        return tempFile;
    }

    public File mergePDFFiles(List<String> encodeStringB64List) throws IOException {

        String fileExtention = "pdf";

        List<File> listFiles = new ArrayList<File>();

        for (int i = 0; i < encodeStringB64List.size(); i++) {

            listFiles.add(transformStringB64ToFile(encodeStringB64List.get(i), fileExtention));

        }

        return mergePDFFiles(listFiles, fileExtention);
    }

    /**
     * Funcion que une varios archivos pdf
     *
     * @return Archivo pdf unido
     */
    public File mergePDFFiles(List<File> listFiles, String fileExtention) {

        try {

        	Path filePath = null;
        	 File mergedFile=null;
            //Instantiating PDFMergerUtility class
            PDFMergerUtility PDFmerger = new PDFMergerUtility();

            FileAttribute<Set<PosixFilePermission>> attributes
		      = PosixFilePermissions.asFileAttribute(new HashSet<>(
		          Arrays.asList(PosixFilePermission.OWNER_WRITE,
		                        PosixFilePermission.OWNER_READ)));
            //Setting the destination file
           
           filePath=Files.createTempFile("merged", ".".concat(fileExtention), attributes);
           mergedFile=filePath.toFile();
            PDFmerger.setDestinationFileName(mergedFile.getAbsolutePath());

            for (File file : listFiles) {

                PDFmerger.addSource(file);
            }

            PDFmerger.mergeDocuments(MemoryUsageSetting.setupMainMemoryOnly());

            return mergedFile;

        } catch (IOException e) {
            // TODO Auto-generated catch block
            logger.info(VentaDigitalUtil.class +
                    "Error Uniendo archivos: " + e.getMessage(), e);
        }

        return null;
    }


   	/**
	 * Método encargado de organizar el objeto VentaDigitalCrediAgil por pasoFuncional
	 * @param ventaDigital
	 * @param pasoFuncional
	 * @return VentaDigitalCrediAgil
	 */
	public VentaDigitalCrediAgil organizarVentaDigital(VentaDigital ventaDigital, String pasoFuncional) {

		VentaDigitalCrediAgil ventaDigitalCrediAgil = new VentaDigitalCrediAgil ();

		ventaDigitalCrediAgil.setSolicitud(ventaDigital.getSolicitud());
		ventaDigitalCrediAgil.setProducto(ventaDigital.getProducto());
		ventaDigitalCrediAgil.setInformacionTransaccion(ventaDigital.getInformacionTransaccion ());
		ventaDigitalCrediAgil.setDatosPersonales(ventaDigital.getDatosPersonales());
		ventaDigitalCrediAgil.setInformacionDispositivo(ventaDigital.getInformacionDispositivo());
		ventaDigitalCrediAgil.setInformacionFinanciera(ventaDigital.getInformacionFinanciera());
		ventaDigitalCrediAgil.setInformacionVivienda(ventaDigital.getInformacionVivienda());
		ventaDigitalCrediAgil.setInformacionLaboral(ventaDigital.getInformacionLaboral());
		ventaDigitalCrediAgil.getInformacionTransaccion ().setPasoFuncional (pasoFuncional);
		ventaDigitalCrediAgil.setInformacionCredito (ventaDigital.getInformacionCredito ());

		return ventaDigitalCrediAgil;
	}

}
