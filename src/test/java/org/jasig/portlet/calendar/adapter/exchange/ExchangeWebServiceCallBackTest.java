/**
 * Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Apereo
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at the
 * following location:
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jasig.portlet.calendar.adapter.exchange;

import static org.junit.Assert.assertTrue;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPException;
import javax.xml.transform.TransformerException;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.ws.client.core.WebServiceMessageCallback;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;

public class ExchangeWebServiceCallBackTest {

  private String impersonatedUser = "impersonatedUser@ed.ac.uk";
  private String actionCallbackType = "actionCallbackType";
  private String requestServerVersion = "requestVersion2007SP1";
  private SoapMessage request;
  private ExchangeWebServiceCallBack testee;
  @Mock private WebServiceMessageCallback actionCallBackMock;
  ByteArrayOutputStream outStream;

  @Before
  public void setUp() throws IOException, SOAPException {
    initMocks(this);
    testee =
        new ExchangeWebServiceCallBack(actionCallbackType, requestServerVersion, impersonatedUser);
    File soapFile = new File("src/test/resources/TestGetAvailabilitySoapMessage.xml");
    InputStream is = new ByteArrayInputStream(FileUtils.readFileToString(soapFile).getBytes());
    request = new SaajSoapMessageFactory(MessageFactory.newInstance()).createWebServiceMessage(is);
    outStream = new ByteArrayOutputStream();
  }

  @Test
  public void testServerVersionAddedAsSoapHeader() throws IOException, TransformerException {
    testee.doWithMessage(request);
    request.writeTo(outStream);
    String resultMessage = outStream.toString();
    // <ns3:RequestServerVersion xmlns:ns3="http://schemas.microsoft.com/exchange/services/2006/types" Version="requestVersion2007SP1"/>
    assertTrue(resultMessage.indexOf("requestVersion2007SP1") != -1);
  }

  @Test
  public void testImpersonationAddedAsSoapHeader() throws IOException, TransformerException {
    testee.doWithMessage(request);
    request.writeTo(outStream);
    String resultMessage = outStream.toString();
    // <t:ExchangeImpersonation xmlns:t="http://schemas.microsoft.com/exchange/services/2006/types"><t:ConnectingSID><t:PrincipalName>impersonatedUser@ed.ac.uk</t:PrincipalName></t:ConnectingSID></t:ExchangeImpersonation>
    assertTrue(resultMessage.indexOf(":ExchangeImpersonation") != -1);
    assertTrue(resultMessage.indexOf(":ConnectingSID") != -1);
    assertTrue(resultMessage.indexOf(":PrincipalName") != -1);
    assertTrue(resultMessage.indexOf(impersonatedUser) != -1);
  }

  @Test
  public void testNoImpersonationAddedAsSoapHeader() throws IOException, TransformerException {
    testee = new ExchangeWebServiceCallBack(actionCallbackType, requestServerVersion, null);
    testee.doWithMessage(request);
    request.writeTo(outStream);
    String resultMessage = outStream.toString();
    // <t:ExchangeImpersonation xmlns:t="http://schemas.microsoft.com/exchange/services/2006/types"><t:ConnectingSID><t:PrincipalName>impersonatedUser@ed.ac.uk</t:PrincipalName></t:ConnectingSID></t:ExchangeImpersonation>
    assertTrue(resultMessage.indexOf(":ExchangeImpersonation") == -1);
    assertTrue(resultMessage.indexOf(":ConnectingSID") == -1);
    assertTrue(resultMessage.indexOf(":PrincipalName") == -1);
    assertTrue(resultMessage.indexOf(impersonatedUser) == -1);
  }
}
