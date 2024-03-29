# k8s-keycloak
[![Clojars Project](https://img.shields.io/clojars/v/dda/k8s-keycloak.svg)](https://clojars.org/dda/k8s-keycloak) [![pipeline status](https://gitlab.com/domaindrivenarchitecture/k8s-keycloak/badges/master/pipeline.svg)](https://gitlab.com/domaindrivenarchitecture/k8s-keycloak/-/commits/master) 

[<img src="https://domaindrivenarchitecture.org/img/delta-chat.svg" width=20 alt="DeltaChat"> chat over e-mail](mailto:buero@meissa-gmbh.de?subject=community-chat) | [<img src="https://meissa-gmbh.de/img/community/Mastodon_Logotype.svg" width=20 alt="team@social.meissa-gmbh.de"> team@social.meissa-gmbh.de](https://social.meissa-gmbh.de/@team) | [Website & Blog](https://domaindrivenarchitecture.org)

## Purpose

k8s-keycloak ....

## Rational

There are many comparable solutions for creating k8s deployments like helm or kustomize. Why do we need another one?
* We like the simplicity of kustomize. Yaml in, yaml out, the ability to lint the result and the option to split large yaml files into objects. But a simple overwriting per environment may not be enough ...
* We like helm packages. A package encapsulates the setup for an application. On the one hand, but on the other hand we don't like the idea of having to program and debug in a template language. We can program much better in real programming languages.

Our k8s-* tools combine the advantages of both approaches:
* Packages for one application
* Programming in clojure
* yaml / edn as input and output, no more magic
* good validation, integration as api, cli or in the browser

## Try out

Click on the image to try out live in your browser:

[![Try it out](/doc/tryItOut.png "Try out yourself")](https://domaindrivenarchitecture.org/pages/dda-provision/k8s-keycloak/)

Your input will stay in your browser. No server interaction is required.

You will also be able to try out on cli:
```
target/graalvm/k8s-keycloak src/test/resources/valid-config.edn src/test/resources/valid-auth.edn | kubeval -
target/graalvm/k8s-keycloak src/test/resources/valid-config.edn src/test/resources/valid-auth.edn | kubectl apply -f -
```

## License

Copyright © 2021 meissa GmbH
Licensed under the [Apache License, Version 2.0](LICENSE) (the "License")
Pls. find licenses of our subcomponents [here](doc/SUBCOMPONENT_LICENSE)