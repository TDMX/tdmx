package org.tdmx.console.application.job;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdmx.client.crypto.algorithm.AsymmetricEncryptionAlgorithm;
import org.tdmx.client.crypto.algorithm.SignatureAlgorithm;
import org.tdmx.console.application.domain.ProblemDO;
import org.tdmx.console.application.domain.ProblemDO.ProblemCode;
import org.tdmx.console.application.service.CertificateAuthorityService;
import org.tdmx.console.application.service.ObjectRegistry;
import org.tdmx.console.application.service.OperationResultHolder;
import org.tdmx.console.application.util.CalendarUtils;
import org.tdmx.console.domain.CertificateAuthorityRequest;

public class DevelopmentInitializationJob extends AbstractBackgroundJob {

	//-------------------------------------------------------------------------
	//PUBLIC CONSTANTS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	//-------------------------------------------------------------------------
	private Logger log = LoggerFactory.getLogger(DevelopmentInitializationJob.class);
	
	private ScheduledExecutorService scheduler = null;
	
	private ObjectRegistry objectRegistry = null;
	private CertificateAuthorityService certificateAuthorityService = null;
	
	private ScheduledFuture<?> future = null;
	
	//-------------------------------------------------------------------------
	//CONSTRUCTORS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PUBLIC METHODS
	//-------------------------------------------------------------------------
	
	@Override
	public void init() {
		scheduler = Executors.newSingleThreadScheduledExecutor();
		Runnable r = new Runnable() {

			@Override
			public void run() {
				initRun();
				try {

					// do something
					if ( getCertificateAuthorityService().search("").size() == 0 ) {
						CertificateAuthorityRequest csr = new CertificateAuthorityRequest();
						csr.setCommonName("Peter Klauser");
						csr.setEmailAddress("pjk@gmail.com");
						csr.setTelephoneNumber("0417100000");
						csr.setOrganization("mycompany.com");
						csr.setCountry("CH");
						
						csr.setNotBefore(new Date());
						csr.setNotAfter(CalendarUtils.getDateWithOffset(new Date(), Calendar.YEAR, 1));
						
						csr.setKeyAlgorithm(AsymmetricEncryptionAlgorithm.RSA4096);
						csr.setSignatureAlgorithm(SignatureAlgorithm.SHA_384_RSA);
						
						OperationResultHolder<String> result = new OperationResultHolder<>();
						
						getCertificateAuthorityService().create(csr.domain(),result);
						if ( result.getError() != null ) {
							ProblemDO p = new ProblemDO(ProblemCode.DEVELOPMENT_INITIALIZATION, result.getError().toString());
							problemRegistry.addProblem(p);							
						} else {
							log.info("Created own CA " + csr.getCertificateAuthorityId());
						}
					}
				} catch ( Throwable t ) {
					log.warn("Unexpected RuntimeException.", t);
					ProblemDO p = new ProblemDO(ProblemCode.RUNTIME_EXCEPTION, t);
					problemRegistry.addProblem(p);							
					throw t;
				} finally {
					finishRun();
				}
			}
			
		};
		// we run 10s to give other jobs a chance to finish, so
		// some objects will be available for us to build on.
		future = scheduler.schedule(r, 10, TimeUnit.SECONDS);
	}

	@Override
	public void shutdown() {
		if ( scheduler != null ) {
			scheduler.shutdown();
			try {
				scheduler.awaitTermination(60, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				// ignore.
			}
		}
		scheduler = null;
		future = null;
	}

	@Override
	public Date getPendingDate() {
		if ( future == null ) {
			return null;
		}
		long seconds = future.getDelay(TimeUnit.SECONDS);
		if ( seconds > 0 ) {
			Calendar c = Calendar.getInstance();
			c.add(Calendar.SECOND, (int)seconds);
			return c.getTime();
		}
		return null;
	}

    //-------------------------------------------------------------------------
	//PROTECTED METHODS
	//-------------------------------------------------------------------------

	@Override
	protected void logInfo(String msg) {
		log.info(msg);
	}

	//-------------------------------------------------------------------------
	//PRIVATE METHODS
	//-------------------------------------------------------------------------
	
	//-------------------------------------------------------------------------
	//PUBLIC ACCESSORS (GETTERS / SETTERS)
	//-------------------------------------------------------------------------

	public ObjectRegistry getObjectRegistry() {
		return objectRegistry;
	}

	public void setObjectRegistry(ObjectRegistry objectRegistry) {
		this.objectRegistry = objectRegistry;
	}

	public CertificateAuthorityService getCertificateAuthorityService() {
		return certificateAuthorityService;
	}

	public void setCertificateAuthorityService(
			CertificateAuthorityService certificateAuthorityService) {
		this.certificateAuthorityService = certificateAuthorityService;
	}

}
