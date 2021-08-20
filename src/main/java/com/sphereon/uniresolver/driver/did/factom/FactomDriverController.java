package com.sphereon.uniresolver.driver.did.factom;

import foundation.identity.did.DID;
import foundation.identity.did.parser.ParserException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uniresolver.ResolutionException;
import uniresolver.result.ResolveDataModelResult;
import uniresolver.result.ResolveRepresentationResult;
import uniresolver.result.ResolveResult;

import java.util.Map;

@RestController("Factom DID resolver")
@RequestMapping(value = "/1.0", name = "Factom DID resolver v1.0")
@Slf4j
public class FactomDriverController {
    private final DIDFactomDriver didFactomDriver;

    public FactomDriverController(DIDFactomDriver didFactomDriver) {
        this.didFactomDriver = didFactomDriver;
    }

    @Operation(summary = "Resolve a DID", operationId = "resolveDID", description = "Resolve an existing DID", tags = "Resolver",
            responses = {@ApiResponse(responseCode = "200", description = "Resolution results", ref = "ResolveDataModelResult")})
    @GetMapping(value = "/identifiers/{did}")
    public ResolveDataModelResult create(@PathVariable(name = "did") String didString) throws ResolutionException {
        try {
            final DID did = DID.fromString(didString, true);
            if (!"factom".equalsIgnoreCase(did.getMethodName())) {
                log.warn("User provided non-supported DID method '{}' in DID {}", did.getMethodName(), didString);
                return ResolveRepresentationResult.makeErrorResult(ResolveResult.ERROR_INVALIDDID,
                        "Factom driver can only resolve factom DIDs. supplied method: " + did.getMethodName(),
                        Map.of(), MediaType.APPLICATION_JSON_VALUE).toResolveDataModelResult();
            }
            return didFactomDriver.resolve(did, Map.of());
        } catch (ParserException e) {
            log.warn(e.getMessage());
            return ResolveRepresentationResult.makeErrorResult(ResolveResult.ERROR_INVALIDDID, e.getMessage(), Map.of(),
                    MediaType.APPLICATION_JSON_VALUE).toResolveDataModelResult();
        }
    }

}
