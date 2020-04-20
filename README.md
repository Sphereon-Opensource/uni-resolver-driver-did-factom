![DIF Logo](https://raw.githubusercontent.com/decentralized-identity/universal-resolver/master/docs/logo-dif.png)

# Universal Resolver Driver: did:factom

This is a [Universal Resolver](https://github.com/decentralized-identity/universal-resolver/) driver for **did:factom** identifiers.

![CI/CD Workflow for driver-did-factom](https://github.com/Sphereon-Opensource/driver-did-factom/workflows/CI/CD%20Workflow%20for%20driver-did-factom/badge.svg?branch=develop)
## Specifications

* [Decentralized Identifiers](https://w3c.github.io/did-core/)
* [Factom DID Method Specification](https://github.com/bi-foundation/FIS/blob/feature/DID/FIS/DID.md)

##

The factom driver uses the [factom-identity-java](https://github.com/Sphereon-Opensource/factom-identity-java) client as
well as [java gson models](https://github.com/Sphereon-Opensource/factom-identity-schema) which are generated from an OpenAPI v3 specification.

A drop in replacement for the universal resolver endpoints is available in the [Factom Identity Server](https://github.com/Sphereon-Opensource/factom-identity-server)

## Example DIDs

```
did:factom:6aa7d4afe4932885b5b6e93accb5f4f6c14bd1827733e05e3324ae392c0b2764

```
## Running the container and configuration
You can run the container using the docker command or using the docker-compose command. 
In either case there are several environment variables to configure runtime parameters

### Environment variables
The universal resolver can connect to multiple networks and nodes. There are 9 of these slots available, and they can be configured using environment variables.
By default the public Factom mainnet and testnet are configured using Factom OpenNode. If you want to disable them, overwrite the values, or disable the slot. The 9 slots denoted by <X> are ranging from 1-9. 

#### NODE<X>_ENABLED
Enables or disables the node. Please see below to disable default OpenNode mainnet and testnet nodes when required (slots 1 and 2 respectively)

#### NODE<X>_NETWORK_ID
Defines the network Id (name). There can only be one network ID per driver instance. If loadbalancing/HA is needed that is the responsibility of the node itself. The network name can be supplied in 2 ways. As a parameter within the request or as part of the DID, eg for testnet.
````
did:factom:testnet:6aa7d4afe4932885b5b6e93accb5f4f6c14bd1827733e05e3324ae392c0b2764
````
The <network_id> part in the did:factom:<network_id>:<chain_id> is completely optional. Default mainnet and testnet can be used for the respective networks. "mainnet" is the default when no network is supplied in the param or DID. For public mainnet the whole :<network_id> part in the DID would be committed., eg:
````
did:factom:6aa7d4afe4932885b5b6e93accb5f4f6c14bd1827733e05e3324ae392c0b2764
````
A request param for the network name always takes precedence over the DID method part.

example: 
````$bash
NODE1_NETWORK_ID=mainnet
````

#### NODE<X>_NETWORK_URL
Defines the [RPC API](https://docs.factomprotocol.org/start/factom-api-docs/factomd-api) URL for the factomd note. Example: https://api.factomd.net/v2

## Configuration
For downloading the dependencies of this project a Personal Access Token for GitHub must be configured in file [settings.xml](https://github.com/sphereon-opensource/uni-resolver-driver-did-factom/blob/master/settings.xml) according to [Creating a personal access token for the command line](https://help.github.com/en/github/authenticating-to-github/creating-a-personal-access-token-for-the-command-line).
Make sure to pass in the following environment variables:
 * GITHUB_READ_PACKAGES_OWNER="<github_user_name>"
 * GITHUB_READ_PACKAGES_TOKEN="<github_access_token_repo_read>"


## Build and Run (Docker)

```
docker build --build-arg GITHUB_READ_PACKAGES_OWNER="<github_username>" --build-arg GITHUB_READ_PACKAGES_TOKEN="<github_token>" -f ./docker/Dockerfile . -t sphereon/driver-did-factom
docker run -p 8080: 8080 sphereon/driver-did-factom
curl -X GET http://localhost:8080/1.0/identifiers/did:factom:6aa7d4afe4932885b5b6e93accb5f4f6c14bd1827733e05e3324ae392c0b2764

```

## Build (native Java)
Maven build:

    export GITHUB_READ_PACKAGES_OWNER="<github_username>" 
    export GITHUB_READ_PACKAGES_TOKEN="<github_token>"
	mvn --settings settings.xml clean install
 
## Build CI/CD (as Github action)
Make sure to have 3 secrets available in Github:
 * DOCKER_USERNAME - The Dockerhub username to push images
 * DOCKER_PASSWORD - The Dockerhub password to push images
 * MAVEN_SETTINGS - The base64 encoded maven settings.xml from this repo, with the GITHUB_READ_PACKAGES variables replaced by your username and personal accesstoken

## Driver Environment Variables

`uniresolver_driver_did_work_apikey` an API Key to allow throttling
`uniresolver_driver_did_work_domain` the URI to call into the Workday Credentials platform

