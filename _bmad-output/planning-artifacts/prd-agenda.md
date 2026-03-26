# Documento de Requisitos do Produto (PRD)

## Visão Geral
Este documento descreve os requisitos para o desenvolvimento de um sistema de agenda telefônica. O objetivo é fornecer uma ferramenta web que permita aos usuários gerenciar seus contatos de forma segura e eficiente.

## Requisitos Funcionais (FRs)

1. **Cadastro de Contatos**:
   - O sistema deve permitir o cadastro de contatos com os seguintes atributos obrigatórios:
     - Nome
     - Sobrenome
     - Data de Nascimento
     - Telefone
   - Cada contato pode ter um ou mais telefones.

2. **Listagem de Contatos**:
   - O sistema deve exibir a lista de contatos do usuário logado.
   - Apenas o usuário logado pode visualizar seus próprios contatos.

3. **Atualização de Contatos**:
   - O sistema deve permitir a atualização dos dados de um contato existente.

4. **Exclusão de Contatos**:
   - O sistema deve permitir a exclusão de contatos.

5. **Mensagens de Sucesso/Erro**:
   - Após cada operação (inserção, alteração ou exclusão), o sistema deve exibir uma mensagem informando o sucesso ou falha da operação.

6. **Autenticação de Usuário**:
   - O sistema deve exigir login com e-mail ou usuário e senha para acessar as funcionalidades.
   - Deve ser possível cadastrar um novo usuário caso não exista.

## Requisitos Não Funcionais (NFRs)

1. **Persistência de Dados**:
   - Os dados devem ser armazenados em uma base de dados.

2. **Linguagem de Programação**:
   - O sistema deve ser desenvolvido em Java.

3. **Plataforma**:
   - O sistema deve ser uma aplicação web.

4. **Testes Unitários**:
   - Todas as funcionalidades devem possuir testes unitários.

## Fluxos de Usuário

1. **Login**:
   - O usuário informa e-mail/usuário e senha para acessar o sistema.
   - Caso não possua cadastro, pode criar uma nova conta.

2. **Gerenciamento de Contatos**:
   - O usuário pode cadastrar, listar, atualizar e excluir contatos.
   - Após cada operação, uma mensagem de feedback é exibida.

## Restrições

1. Todos os dados obrigatórios (*) devem ser preenchidos pelo usuário.
2. Apenas o usuário logado pode acessar seus contatos.
3. O sistema deve tratar erros de forma amigável e sem expor informações sensíveis.