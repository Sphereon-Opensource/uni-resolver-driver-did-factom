package com.sphereon.uniresolver.driver.did.factom;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uniresolver.ResolutionException;
import uniresolver.result.ResolveResult;

@RestController("Factom DID resolver")
@RequestMapping(value = "/1.0", name = "Factom DID resolver v1.0")
public class FactomDriverController {
    private final DIDFactomDriver didFactomDriver;

    public FactomDriverController(DIDFactomDriver didFactomDriver) {
        this.didFactomDriver = didFactomDriver;
    }

    @Operation(summary = "Resolve a DID", operationId = "resolveDID", description = "Resolve an existing DID", tags = "Resolver")
    @PostMapping(value = "/identifiers/{id}")
    public ResolveResult create(@PathVariable(name = "id") String id) throws ResolutionException {
        return didFactomDriver.resolve(id);
    }

}
