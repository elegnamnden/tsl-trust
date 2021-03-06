/*
 * Copyright 2017 Swedish E-identification Board (E-legitimationsnämnden)
 *  		 
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 * 
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 * 
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package se.tillvaxtverket.tsltrust.common.tsl;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import javax.xml.parsers.ParserConfigurationException;

import com.aaasec.lib.crypto.xml.SigVerifyResult;
import com.aaasec.lib.crypto.xml.XMLSign;
import com.aaasec.lib.crypto.xml.XmlUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.xml.security.utils.XMLUtils;
import org.etsi.uri.x02231.v2.InternationalNamesType;
import org.etsi.uri.x02231.v2.MultiLangNormStringType;
import org.etsi.uri.x02231.v2.MultiLangStringType;
import org.etsi.uri.x02231.v2.NonEmptyMultiLangURIType;
import org.etsi.uri.x02231.v2.OtherTSLPointerType;
import org.etsi.uri.x02231.v2.PostalAddressType;
import org.etsi.uri.x02231.v2.TSPType;
import org.etsi.uri.x02231.v2.TrustServiceProviderListType;
import org.etsi.uri.x02231.v2.TrustStatusListType;
import org.xml.sax.SAXException;

/**
 * Java object for XML parsing trust service status lists
 */
public class TrustServiceList {

    private TrustStatusListType tsl;
    private byte[] tslBytes;
    private String sha1Fingerprint = "";
    private List<TrustServiceProvider> tspList = new ArrayList<TrustServiceProvider>();

    public TrustServiceList(TrustStatusListType tslData, byte[] tslBytes) {
        this.tsl = tslData;
        this.tslBytes = tslBytes;
        if (tslBytes != null) {
            sha1Fingerprint = DigestUtils.shaHex(tslBytes);
        }
        try {
            TrustServiceProviderListType tspListElm = tsl.getTrustServiceProviderList();
            TSPType[] tspTypeList = tspListElm.getTrustServiceProviderArray();
            for (TSPType tsp : tspTypeList) {
                tspList.add(new TrustServiceProvider(tsp));
            }
        } catch (Exception ex) {
        }
    }


    /**
     * Getter for the TSL java object
     * @return TSL java object
     */
    public TrustStatusListType getTslData() {
        return tsl;
    }

    /**
     * @return The bytes of the TSL XML document;
     */
    public byte[] getBytes(){
        return tslBytes;
    }
    /**
     * @return a list of Trust Service Providers in the TSL
     */
    public List<TrustServiceProvider> getTrustServiceProviders() {
        return tspList;
    }

    /**
     * SHA1 fingerprint of the TSL XML binary data
     * @return text representation of SHA1 hash
     */
    public String getSha1Fingerprint() {
        return sha1Fingerprint;
    }

    /**
     * @return TSL Scheme territory 
     */
    public String getSchemeTerritory() {
        String data = "";
        try {
            data = tsl.getSchemeInformation().getSchemeTerritory();
        } catch (Exception ex) {
        }
        return data;
    }

    /**
     * @return TSL Scheme name
     */
    public String getSchemeName() {
        return getSchemeName(Locale.ENGLISH);
    }

    /**
     * @param locale preferred language
     * @return TSL Scheme name
     */
    public String getSchemeName(Locale locale) {
        String data = "";
        try {
            InternationalNamesType schemeName = tsl.getSchemeInformation().getSchemeName();
            data = TslUtils.getLocalisedNormString(schemeName.getNameArray(), locale);
        } catch (Exception ex) {
        }
        return data;
    }

    /**
     * @return Other TSL pointers located in the TSL scheme information
     */
    public List<OtherTSLPointerData> getOtherTSLPointers() {
        List<OtherTSLPointerData> data = new ArrayList<OtherTSLPointerData>();
        try {
            OtherTSLPointerType[] otherTSLPointerArray = tsl.getSchemeInformation().getPointersToOtherTSL().getOtherTSLPointerArray();
            for (OtherTSLPointerType otp:otherTSLPointerArray){
                data.add(new OtherTSLPointerData(otp));
            }
        } catch (Exception ex) {
        }
        return data;
    }
    

    /**
     * Verifies the signature on the TSL
     * @return Signature verification result object
     */
    public SigVerifyResult verifySignature() throws ParserConfigurationException, SAXException, IOException {
        return XMLSign.verifySignature(tslBytes);
    }

    /**
     * @return true if the TSL has a signature element
     */
    public boolean hasSignature() {
        try {
            XMLSign.XmlSigData signatureData = XMLSign.getSignatureData(XmlUtils.getDocument(tslBytes));
            return (signatureData.sigType != null);
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * @return Date when the TSL is scheduled to be updated
     */
    public Date getNextUpdate() {
        Date data = null;
        try {
            data = tsl.getSchemeInformation().getNextUpdate().getDateTime().getTime();
        } catch (Exception ex) {
        }
        return data;
    }

    /**
     * @return Date when the TSL was issued
     */
    public Date getIssueDate() {
        Date data = null;
        try {
            data = tsl.getSchemeInformation().getListIssueDateTime().getTime();
        } catch (Exception ex) {
        }
        return data;
    }

    public BigInteger getSequenceNumber() {
        BigInteger data = null;
        try {
            data = tsl.getSchemeInformation().getTSLSequenceNumber();
        } catch (Exception ex) {
        }
        return data;
    }

    public String getSchemeOperatorName() {
        return getSchemeOperatorName(Locale.ENGLISH);
    }

    public String getSchemeOperatorName(Locale locale) {
        String data = "";
        try {
            MultiLangNormStringType[] name = tsl.getSchemeInformation().getSchemeOperatorName().getNameArray();
            data = TslUtils.getLocalisedNormString(name, locale);
        } catch (Exception ex) {
        }
        return data;
    }

    public String getStatusDeterminationApproach() {
        String data = "";
        try {
            data = tsl.getSchemeInformation().getStatusDeterminationApproach();
        } catch (Exception ex) {
        }
        return data;
    }

    public String getType() {
        String data = "";
        try {
            data = tsl.getSchemeInformation().getTSLType();
        } catch (Exception ex) {
        }
        return data;
    }

    public List<String> getSchemeInformationUris() {
        List<String> data = new ArrayList<String>();
        try {
            List<NonEmptyMultiLangURIType> uriList = Arrays.asList(tsl.getSchemeInformation().getSchemeInformationURI().getURIArray());
            for (NonEmptyMultiLangURIType mlut : uriList) {
                data.add(mlut.getStringValue());
            }
        } catch (Exception ex) {
        }
        return data;
    }

    public List<String> getSchemeOperatorElectronicAddresses() {
        List<String> data = new ArrayList<String>();
        try {
            data = Arrays.asList(tsl.getSchemeInformation().getSchemeOperatorAddress().getElectronicAddress().getURIArray());
        } catch (Exception ex) {
        }
        return data;
    }

    public PostalAddressType getSchemeOperatorPostalAddress() {
        return getSchemeOperatorPostalAddress(Locale.ENGLISH);
    }

    public PostalAddressType getSchemeOperatorPostalAddress(Locale locale) {
        PostalAddressType data = null;
        try {
            PostalAddressType[] paList = tsl.getSchemeInformation().getSchemeOperatorAddress().getPostalAddresses().getPostalAddressArray();
            PostalAddressType targetPa = null, defPa = null;
            for (PostalAddressType pa : paList) {
                if (pa.getLang().equalsIgnoreCase(locale.getLanguage())) {
                    targetPa = pa;
                }
                if (pa.getLang().toLowerCase(locale).startsWith(Locale.ENGLISH.getLanguage())) {
                    defPa = pa;
                }
            }
            data = (targetPa != null) ? targetPa : defPa;
        } catch (Exception ex) {
        }
        return data;
    }

    public List<String> getSchemeTypes() {
        List<String> data = new ArrayList<String>();
        try {
            data = Arrays.asList(tsl.getSchemeInformation().getSchemeTypeCommunityRules().getURIArray());
        } catch (Exception ex) {
        }
        return data;
    }

    public String getLegalNotice() {
        return getLegalNotice(Locale.ENGLISH);
    }

    public String getLegalNotice(Locale locale) {
        String data = "";
        try {
            MultiLangStringType[] lnList = tsl.getSchemeInformation().getPolicyOrLegalNotice().getTSLLegalNoticeArray();
            data = TslUtils.getLocalisedString(lnList, locale);
        } catch (Exception ex) {
        }
        return data;
    }
}
