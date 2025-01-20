# convention 4 kubernetes: c4k-keycloak
[![Clojars Project](https://img.shields.io/clojars/v/org.domaindrivenarchitecture/c4k-keycloak.svg)](https://clojars.org/org.domaindrivenarchitecture/c4k-keycloak) [![pipeline status](https://gitlab.com/domaindrivenarchitecture/c4k-keycloak/badges/master/pipeline.svg)](https://gitlab.com/domaindrivenarchitecture/c4k-keycloak/-/commits/master) 

[<img src="https://domaindrivenarchitecture.org/img/delta-chat.svg" width=20 alt="DeltaChat"> chat over e-mail](mailto:buero@meissa-gmbh.de?subject=community-chat) | [<img src="https://meissa.de/images/parts/contact/mastodon36_hue9b2464f10b18e134322af482b9c915e_5501_filter_14705073121015236177.png" width=20 alt="M"> meissa@social.meissa-gmbh.de](https://social.meissa-gmbh.de/@meissa) | [Blog](https://domaindrivenarchitecture.org) | [Website](https://meissa.de)

## Purpose

c4k-keycloak provides a k8s deployment for keycloak containing:
* keycloak
* ingress having a letsencrypt managed certificate
* postgres database

The package aims to a low load sceanrio.

## Status

This is just a POC, database is stored volatile, there is no backup implemented.

## Try out

Click on the image to try out live in your browser:

[![Try it out](/doc/tryItOut.png "Try out yourself")](https://domaindrivenarchitecture.org/pages/dda-provision/c4k-keycloak/)

Your input will stay in your browser. No server interaction is required.

You will also be able to try out on cli:
```
target/graalvm/c4k-keycloak src/test/resources/keycloak-test/valid-config.edn src/test/resourceskeycloak-test/valid-auth.edn | kubeval -
target/graalvm/c4k-keycloak src/test/resources/keycloak-test/valid-config.edn src/test/resources/keycloak-test/valid-auth.edn | kubectl apply -f -
```

## Development & mirrors

Development happens at: https://repo.prod.meissa.de/meissa/c4k-keycloak

Mirrors are:

* https://codeberg.org/meissa/c4k-keycloak.git
* https://gitlab.com/domaindrivenarchitecture/c4k-keycloak (issues and PR, CI)
* https://github.com/DomainDrivenArchitecture/c4k-keycloak

For more details about our repository model see: https://repo.prod.meissa.de/meissa/federate-your-repos

## License

Copyright © 2024 meissa GmbH
Licensed under the [Apache License, Version 2.0](LICENSE) (the "License")
Pls. find licenses of our subcomponents [here](doc/SUBCOMPONENT_LICENSE)
