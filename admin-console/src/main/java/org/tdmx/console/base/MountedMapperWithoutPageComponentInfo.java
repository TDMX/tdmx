package org.tdmx.console.base;

import org.apache.wicket.core.request.handler.ListenerInterfaceRequestHandler;
import org.apache.wicket.core.request.mapper.MountedMapper;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.component.IRequestablePage;
import org.apache.wicket.request.mapper.info.PageComponentInfo;
import org.apache.wicket.request.mapper.parameter.PageParametersEncoder;

public class MountedMapperWithoutPageComponentInfo extends MountedMapper {

	  public MountedMapperWithoutPageComponentInfo(String mountPath, Class<? extends IRequestablePage> pageClass) {
	    super(mountPath, pageClass, new PageParametersEncoder());
	  }

	  @Override
	  protected void encodePageComponentInfo(Url url, PageComponentInfo info) {
	    // do nothing so that component info does not get rendered in url
	  }

	  @Override 
	  public Url mapHandler(IRequestHandler requestHandler) 
	  { 
	      if (requestHandler instanceof ListenerInterfaceRequestHandler) { 
	          return null; 
	      } else { 
	           return super.mapHandler(requestHandler); 
	      } 
	  } 
	}