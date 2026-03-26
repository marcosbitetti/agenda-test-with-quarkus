# Fluxos de UX - Sistema de Agenda Telefônica

## Visão Geral
Este documento descreve os fluxos de interação do usuário para as principais funcionalidades do sistema de agenda telefônica. O objetivo é garantir uma experiência de usuário intuitiva e eficiente.

## Fluxo 1: Login e Cadastro de Usuário

### Cenário 1.1: Login de Usuário
1. O usuário acessa a tela inicial do sistema.
2. O sistema exibe um formulário com os campos:
   - E-mail ou Usuário
   - Senha
3. O usuário preenche os campos e clica no botão "Entrar".
4. O sistema valida as credenciais:
   - **Sucesso**: Redireciona para a tela de listagem de contatos.
   - **Erro**: Exibe uma mensagem genérica de falha no login.

### Cenário 1.2: Cadastro de Novo Usuário
1. O usuário acessa a tela inicial e clica no botão "Cadastrar-se".
2. O sistema exibe um formulário com os campos:
   - Nome
   - E-mail
   - Usuário
   - Senha
3. O usuário preenche os campos e clica no botão "Cadastrar".
4. O sistema valida os dados:
   - **Sucesso**: Redireciona para a tela de login com uma mensagem de sucesso.
   - **Erro**: Exibe mensagens indicando os campos inválidos.

## Fluxo 2: Gerenciamento de Contatos

### Cenário 2.1: Cadastro de Contato
1. O usuário logado acessa a tela de listagem de contatos e clica no botão "Novo Contato".
2. O sistema exibe um formulário com os campos:
   - Nome
   - Sobrenome
   - Data de Nascimento
   - Telefone(s)
   - Grau de Parentesco (opcional)
3. O usuário preenche os campos e clica no botão "Salvar".
4. O sistema valida os dados:
   - **Sucesso**: Exibe uma mensagem de sucesso e atualiza a lista de contatos.
   - **Erro**: Exibe mensagens indicando os campos inválidos.

### Cenário 2.2: Atualização de Contato
1. O usuário logado seleciona um contato na lista e clica no botão "Editar".
2. O sistema exibe o formulário preenchido com os dados do contato.
3. O usuário altera os dados e clica no botão "Salvar".
4. O sistema valida os dados:
   - **Sucesso**: Exibe uma mensagem de sucesso e atualiza a lista de contatos.
   - **Erro**: Exibe mensagens indicando os campos inválidos.

### Cenário 2.3: Exclusão de Contato
1. O usuário logado seleciona um contato na lista e clica no botão "Excluir".
2. O sistema exibe uma mensagem de confirmação.
3. O usuário confirma a exclusão.
4. O sistema realiza a operação:
   - **Sucesso**: Exibe uma mensagem de sucesso e atualiza a lista de contatos.
   - **Erro**: Exibe uma mensagem de erro genérica.

## Fluxo 3: Mensagens de Feedback

### Cenário 3.1: Mensagens de Sucesso
- Após operações bem-sucedidas (ex.: cadastro, atualização, exclusão), o sistema exibe uma mensagem de sucesso no topo da tela.

### Cenário 3.2: Mensagens de Erro
- Em caso de falha, o sistema exibe mensagens de erro claras e genéricas, sem expor detalhes técnicos.

---

Este documento será atualizado conforme novos fluxos forem identificados ou refinados.