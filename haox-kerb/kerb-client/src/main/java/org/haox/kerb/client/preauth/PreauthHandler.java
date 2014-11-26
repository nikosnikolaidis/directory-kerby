package org.haox.kerb.client.preauth;

import org.haox.kerb.client.KrbContext;
import org.haox.kerb.client.KrbOptions;
import org.haox.kerb.client.preauth.pkinit.PkinitPreauth;
import org.haox.kerb.client.preauth.token.TokenPreauth;
import org.haox.kerb.client.request.KdcRequest;
import org.haox.kerb.codec.KrbCodec;
import org.haox.kerb.spec.KrbException;
import org.haox.kerb.spec.type.common.EtypeInfo;
import org.haox.kerb.spec.type.common.EtypeInfo2;
import org.haox.kerb.spec.type.pa.PaData;
import org.haox.kerb.spec.type.pa.PaDataEntry;
import org.haox.kerb.spec.type.pa.PaDataType;

import java.util.ArrayList;
import java.util.List;

public class PreauthHandler {

    private List<KrbPreauth> preauths;
    private PreauthCallback preauthCallback;

    public void init(KrbContext krbContext) {
        preauthCallback = new PreauthCallback();
        loadPreauthPlugins(krbContext);
    }

    private void loadPreauthPlugins(KrbContext context) {
        preauths = new ArrayList<KrbPreauth>();

        KrbPreauth preauth = new TimestampPreauth();
        preauth.init(context);
        preauths.add(preauth);

        preauth = new PkinitPreauth();
        preauth.init(context);
        preauths.add(preauth);

        preauth = new TokenPreauth();
        preauth.init(context);
        preauths.add(preauth);
    }

    public PreauthContext preparePreauthContext(KrbContext krbContext, KdcRequest kdcRequest) {
        PreauthContext preauthContext = new PreauthContext();

        for (KrbPreauth preauth : preauths) {
            PreauthHandle handle = new PreauthHandle();
            handle.preauth = preauth;
            handle.requestContext = preauth.initRequestContext(
                    krbContext, kdcRequest, preauthCallback);
            preauthContext.getHandles().add(handle);
        }

        return preauthContext;
    }

    public void preauth(KrbContext krbContext, KdcRequest kdcRequest) throws KrbException {
        PreauthContext preauthContext = kdcRequest.getPreauthContext();

        if (!preauthContext.isPreauthRequired()) {
            return;
        }

        if (!preauthContext.hasInputPaData()) {
            return;
        }

        attemptETypeInfo(krbContext, kdcRequest, preauthContext.getInputPaData());

        setPreauthOptions(krbContext, kdcRequest, kdcRequest.getPreauthOptions());

        prepareUserResponses(krbContext, kdcRequest, preauthContext.getInputPaData());

        preauthContext.getUserResponser().respondQuestions();

        if (!kdcRequest.isRetrying()) {
            process(krbContext, kdcRequest, null, preauthContext.getInputPaData());
        } else {
            tryAgain(krbContext, kdcRequest, null, null, preauthContext.getInputPaData());
        }
    }

    public void prepareUserResponses(KrbContext krbContext, KdcRequest kdcRequest,
                                     PaData inPadata) throws KrbException {
        PreauthContext preauthContext = kdcRequest.getPreauthContext();

        for (PaDataEntry pae : inPadata.getElements()) {
            if (! preauthContext.isPaTypeAllowed(pae.getPaDataType())) {
                continue;
            }

            PreauthHandle handle = findHandle(krbContext, kdcRequest, pae.getPaDataType());
            if (handle == null) {
                continue;
            }

            handle.prepareQuestions(krbContext, kdcRequest, preauthCallback);
        }
    }

    public void setPreauthOptions(KrbContext krbContext, KdcRequest kdcRequest,
                                  KrbOptions preauthOptions) throws KrbException {
        PreauthContext preauthContext = kdcRequest.getPreauthContext();
        for (PreauthHandle handle : preauthContext.getHandles()) {
            handle.setPreauthOptions(krbContext, kdcRequest, preauthCallback, preauthOptions);
        }
    }

    public void process(KrbContext krbContext, KdcRequest kdcRequest,
                        PaDataEntry inPadata, PaData outPadata) throws KrbException {
        PreauthContext preauthContext = kdcRequest.getPreauthContext();
        for (PreauthHandle handle : preauthContext.getHandles()) {
            handle.process(krbContext, kdcRequest, preauthCallback, inPadata, outPadata);
        }
    }

    public void tryAgain(KrbContext krbContext, KdcRequest kdcRequest, PaDataType preauthType,
                         PaData errPadata, PaData outPadata) {
        PreauthContext preauthContext = kdcRequest.getPreauthContext();
        for (PreauthHandle handle : preauthContext.getHandles()) {
            handle.tryAgain(krbContext, kdcRequest, preauthCallback,
                    preauthType, errPadata, outPadata);
        }
    }

    public void destroy(KrbContext krbContext) {
        for (KrbPreauth preauth : preauths) {
            preauth.destroy(krbContext);
        }
    }

    private PreauthHandle findHandle(KrbContext krbContext, KdcRequest kdcRequest,
                                     PaDataType paType) {
        PreauthContext preauthContext = kdcRequest.getPreauthContext();

        for (PreauthHandle handle : preauthContext.getHandles()) {
            for (PaDataType pt : handle.preauth.getPaTypes()) {
                if (pt == paType) {
                    return handle;
                }
            }
        }
        return null;
    }

    private void attemptETypeInfo(KrbContext krbContext, KdcRequest kdcRequest,
                                  PaData inPadata) throws KrbException {
        PreauthContext preauthContext = kdcRequest.getPreauthContext();

        // Find an etype-info2 or etype-info element in padata
        EtypeInfo etypeInfo = null;
        EtypeInfo2 etypeInfo2 = null;
        PaDataEntry pae = inPadata.findEntry(PaDataType.ETYPE_INFO);
        if (pae != null) {
            etypeInfo = KrbCodec.decode(pae.getPaDataValue(), EtypeInfo.class);
        } else {
            pae = inPadata.findEntry(PaDataType.ETYPE_INFO2);
            if (pae != null) {
                etypeInfo2 = KrbCodec.decode(pae.getPaDataValue(), EtypeInfo2.class);
            }
        }

        if (etypeInfo == null && etypeInfo2 == null) {
            attemptSalt(krbContext, kdcRequest, inPadata);
        }


    }

    private void attemptSalt(KrbContext krbContext, KdcRequest kdcRequest,
                                  PaData inPadata) throws KrbException {

    }
}
