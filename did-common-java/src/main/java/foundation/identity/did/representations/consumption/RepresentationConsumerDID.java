package foundation.identity.did.representations.consumption;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import foundation.identity.did.representations.Representations;

public class RepresentationConsumerDID extends AbstractRepresentationConsumer implements RepresentationConsumer {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final RepresentationConsumerDID instance = new RepresentationConsumerDID();

    public static RepresentationConsumerDID getInstance() {
        return instance;
    }

    private RepresentationConsumerDID() {
        super(Representations.MEDIA_TYPE_DID);
    }

    @Override
    public Result consume(byte[] representation) throws IOException {
        Map<String, Object> map = objectMapper.readValue(representation, LinkedHashMap.class);
        Result result = this.detectRepresentationSpecificEntries(map);
        Map<String,Object> did = result.didDocument();
        Map<String, Map<String, Object>> entries = result.representationSpecificEntries();
        entries.putIfAbsent(Representations.MEDIA_TYPE_DID, Collections.emptyMap());
        return new Result(did, entries);
    }
}
