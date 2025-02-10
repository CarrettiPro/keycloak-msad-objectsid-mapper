# Keycloak MSAD ObjectSID Mapper

This is a LDAP mapper for Keycloak that allows to decode Microsoft Active Directory ObjectSID binary representations and map them into user attributes.

## Build

```
mvn clean install
```

## Install

```
cp target/keycloak-msad-objectsid-mapper-1.0.0-SNAPSHOT.jar /path/to/keycloak/providers
```

### With Docker

#### Use with the official image

```
docker run -v $(pwd)/target/keycloak-msad-objectsid-mapper-1.0.0-SNAPSHOT.jar:/opt/keycloak/providers/keycloak-msad-objectsid-mapper-1.0.0-SNAPSHOT.jar quay.io/keycloak/keycloak start-dev
```

#### Build a custom image

```
FROM quay.io/keycloak/keycloak:latest

COPY target/keycloak-msad-objectsid-mapper-1.0.0-SNAPSHOT.jar /opt/keycloak/providers/
```

## Configure
Go to User Federation → (your LDAP provider) → Mappers, then add a mapper of type `msad-objectsid-ldap-mapper`:
    
![Configure objectSID mapper](https://github.com/user-attachments/assets/7ab222f5-25c5-4103-a85c-b4c996d6bb87)

## Use
You should be able to see the decoded ObjectSID as a user attribute:
    
![ObjectSID attribute](https://github.com/user-attachments/assets/55f5793f-cb8c-4fc7-bbd1-a5c6578d8fc5)

Now, you can map this attribute into protocol-specific objects.

### OpenID Connect
Map to a JWT token claim:
    
![Map ObjectSID to a JWT token claim](https://github.com/user-attachments/assets/7f63977d-9580-475c-bd7d-fd4c13d15106)

Example ID token:
```json
{
  "exp": 1739147960,
  "iat": 1739147660,
  "jti": "6ce037d1-6ebf-4039-ae2c-967a6c517201",
  "iss": "http://localhost:8080/realms/test",
  "aud": "test",
  "sub": "94c183ef-d489-400a-8450-2f79cfba6960",
  "typ": "ID",
  "azp": "test",
  "sid": "35677f39-bd73-4ade-93cd-dbe79234ab2a",
  "at_hash": "vZ2z1TnNW1KWhIhPJ6tPkw",
  "acr": "1",
  "email_verified": false,
  "objsid": "S-1-5-21-2366650763-2036184774-1171199138-1502",
  "name": "Alice Liddell",
  "preferred_username": "alice",
  "given_name": "Alice",
  "family_name": "Liddell",
  "email": "alice@wonder.land"
}
```

### SAML
Map to a SAML assertion attribute:

![Map to a SAML assertion attribute](https://github.com/user-attachments/assets/1fb124ad-aae1-4690-be13-78d580090ff6)

Example SAML assertion:
```xml
<saml:Assertion xmlns="urn:oasis:names:tc:SAML:2.0:assertion" ID="ID_6b841b36-7754-4a02-91b4-96a01b7e69d5" IssueInstant="2025-02-10T00:46:47.874Z" Version="2.0">
        <saml:Issuer>
            http://localhost:8080/realms/test
        </saml:Issuer>
        <saml:Subject>
            <saml:NameID Format="urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified">
                alice
            </saml:NameID>
            <saml:SubjectConfirmation Method="urn:oasis:names:tc:SAML:2.0:cm:bearer">
                <saml:SubjectConfirmationData InResponseTo="_b0b02821e77348dbb5261207263bb81d" NotOnOrAfter="2025-02-10T00:51:45.874Z" Recipient="https://samlmock.dev/callback"/>
            </saml:SubjectConfirmation>
        </saml:Subject>
        ...
        <saml:AttributeStatement>
            <saml:Attribute FriendlyName="objectSID" Name="objectSID" NameFormat="urn:oasis:names:tc:SAML:2.0:attrname-format:basic">
                <saml:AttributeValue xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="xs:string">
                    S-1-5-21-2366650763-2036184774-1171199138-1502
                </saml:AttributeValue>
            </saml:Attribute>
            ...
        </saml:AttributeStatement>
    </saml:Assertion>
```
