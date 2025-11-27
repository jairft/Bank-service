# 🏦 **Banco Nexo — Sistema Bancário Digital Distribuído**

```
██████╗  █████╗ ███╗   ██╗ ██████╗ ██████╗     ███╗   ██╗███████╗██╗  ██╗ ██████╗ 
██╔══██╗██╔══██╗████╗  ██║██╔════╝ ██╔══██╗    ████╗  ██║██╔════╝██║  ██║██╔═══██╗
██████╔╝███████║██╔██╗ ██║██║  ███╗██████╔╝    ██╔██╗ ██║█████╗  ███████║██║   ██║
██╔═══╝ ██╔══██║██║╚██╗██║██║   ██║██╔══██╗    ██║╚██╗██║██╔══╝  ██╔══██║██║   ██║
██║     ██║  ██║██║ ╚████║╚██████╔╝██║  ██║    ██║ ╚████║███████╗██║  ██║╚██████╔╝
╚═╝     ╚═╝  ╚═╝╚═╝  ╚═══╝ ╚═════╝ ╚═╝  ╚═╝    ╚═╝  ╚═══╝╚══════╝╚═╝  ╚═╝ ╚═════╝ 
```

<p align="center">
  <img src="https://img.shields.io/badge/build-passing-22c55e?style=for-the-badge">
  <img src="https://img.shields.io/badge/Java-17-0A7EB5?style=for-the-badge">
  <img src="https://img.shields.io/badge/Spring_Boot-3.x-6DB33F?style=for-the-badge">
  <img src="https://img.shields.io/badge/Angular-16+-DD0031?style=for-the-badge">
  <img src="https://img.shields.io/badge/Kafka-Event_Driven-231F20?style=for-the-badge">
  <img src="https://img.shields.io/badge/Eureka_Service_Discovery-4285F4?style=for-the-badge">
  <img src="https://img.shields.io/badge/API_Gateway-Spring_Cloud_Gateway-0F172A?style=for-the-badge">
</p>

---

# 📌 **Visão Geral**

O **Banco Nexo** é um sistema bancário digital completo, desenvolvido com microsserviços Spring Boot, arquitetura orientada a eventos (Kafka), autenticação JWT e frontend Angular.

### Recursos:
✔ Login + autenticação JWT  
✔ Cadastro de usuários  
✔ Ativação de conta  
✔ Dashboard com saldo e histórico  
✔ Depósitos  
✔ PIX (transferência)  
✔ Cadastro de chaves PIX  
✔ Senha transacional  
✔ Solicitação de cartão  
✔ Perfil do usuário  
✔ Eventos assíncronos via Kafka  

---

# 🧩 **Arquitetura Completa**

```
                          ┌──────────────────────────┐
                          │        Angular SPA       │
                          │  (web-banking frontend)  │
                          └─────────────┬────────────┘
                                        │ HTTP
                                        ▼
                              ┌──────────────────┐
                              │   API Gateway    │
                              │ JWT | Routing | CORS │
                              └─────────┬────────┘
                       ┌────────────────┼──────────────────┐
                       ▼                ▼                  ▼
             ┌────────────────┐ ┌────────────────┐ ┌───────────────────┐
             │ Auth Service   │ │ User Service   │ │ Account Service   │
             │ Login/JWT      │ │ CRUD usuário   │ │ PIX / Depósitos   │
             └────────────────┘ └────────────────┘ └───────────────────┘
                       │                │                  │
                       └─────── Kafka Topics (Events) ─────┘
                                     user-events
                                     user-updated-events
                                     account-events
```

---

# 🚀 **Como Executar (SEM DOCKER!)**

Você rodará **cada microsserviço manualmente**.

---

## 1️⃣ Clonar o projeto

```bash
git clone [https://github.com/seu-usuario/banco-nexo.git](https://github.com/jairft/Bank-service)
cd banco-nexo
```

---

# 2️⃣ Subir o Kafka manualmente

Você precisa ter **Kafka + Zookeeper** instalados localmente.

### Start Zookeeper:

```bash
zookeeper-server-start.sh config/zookeeper.properties
```

### Start Kafka:

```bash
kafka-server-start.sh config/server.properties
```

---

# 3️⃣ Criar os tópicos necessários

```bash
kafka-topics.sh --create --topic user-events --bootstrap-server localhost:9092
kafka-topics.sh --create --topic user-updated-events --bootstrap-server localhost:9092
kafka-topics.sh --create --topic account-events --bootstrap-server localhost:9092
```

---

# 4️⃣ Executar cada serviço Spring Boot

Em terminais separados:

### Eureka Server:

```bash
cd eureka-server
./mvnw spring-boot:run
```

### API Gateway:

```bash
cd api-gateway
./mvnw spring-boot:run
```

### Auth Service:

```bash
cd auth-service
./mvnw spring-boot:run
```

### User Service:

```bash
cd user-service
./mvnw spring-boot:run
```

### Account Service:

```bash
cd account-service
./mvnw spring-boot:run
```

---

# 5️⃣ Rodar o Frontend Angular

```bash
cd web-banking
npm install
ng serve
```

Acesse:  
👉 **http://localhost:4200**

---

# 🔐 **Segurança**

✔ JWT Bearer protegido no Gateway  
✔ BCrypt para senhas  
✔ Senha transacional obrigatória  
✔ Logs centralizados  
✔ Eventos críticos feitos via Kafka  

---

# 🖼️ **Screenshots da Aplicação**

> Coloque suas imagens nesta pasta:  
📁 **docs/screenshots/**


| Tela | Imagem |
|------|--------|
| Login | ![](docs/screenshots/login.png) |
| Ativação | ![](docs/screenshots/ativar-conta.png) |
| Dashboard | ![](docs/screenshots/dashboard.png) |
| Depósito | ![](docs/screenshots/deposito.png) |
| Senha Transacional | ![](docs/screenshots/senha-transacional.png) |
| Perfil | ![](docs/screenshots/perfil.png) |
| Chaves PIX | ![](docs/screenshots/pix-chaves.png) |
| Cartão | ![](docs/screenshots/solicitar-cartao.png) |

---

# 🧬 **Estrutura do Repositório**

```
/banco-nexo
   /api-gateway
   /eureka-server
   /auth-service
   /user-service
   /account-service
   /web-banking
   /docs
      /screenshots
   README.md
```

---

# 📜 **Roadmap**

- [ ] Testes unitários JUnit  
- [ ] Testes integrados dos serviços  
- [ ] Métricas com Spring Actuator  
- [ ] Logs distribuídos com ELK  
- [ ] UI para admin  
- [ ] WebSocket para atualizações de saldo  

---

# 👤 **Autor**

Feito com 💜 por **Jair Freitas**.  
Se quiser melhorar este projeto, pull requests são bem-vindos!

---

