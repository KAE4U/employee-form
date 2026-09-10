# employee-form

A desktop **employee registration form** (Cadastro de Funcionários) built with **Java Swing** and styled with the [FlatLaf](https://www.formdev.com/flatlaf/) dark look-and-feel. The interface is organized into tabs and mirrors a typical Brazilian HR/payroll employee record, including personal data, documents, contract details, and operational information.

> The UI labels and content are in Portuguese (pt-BR).

## Screenshot / Overview

The application opens a single window (`Funcionários`) with:

- A **status bar** at the top with action buttons: `Salvar`, `Concluir`, `Excluir`, `Ocorrência`, and `Fechar`, plus live **Status** and **Situação** indicators.
- A **CPF / Nome** header row.
- Four **tabs**:
  - **Principal** — Dados Gerais, Endereço, Dados Pessoais, and Filiação.
  - **Documentação** — RG, CPF, CTPS, PIS, Título de Eleitor, CNH, Informação Militar, Conselho Regional, and RIC.
  - **Contrato** — Vínculo, admissão, cargo, salário, FGTS, experiência, and rescisão details.
  - **Operacional** — benefícios (INSS, FGTS, IRRF, etc.), sindicato, dados bancários, exames admissionais, and a **photo picker**.

## Features

- Tabbed layout for organizing a large number of employee fields.
- **Ocorrências** dialog to add timestamped occurrence/notes entries for the employee.
- **Photo upload** with a custom rounded-corner panel and a placeholder avatar when no photo is set (supports `.jpg`, `.jpeg`, `.png`, `.gif`).
- Confirmation dialogs for destructive actions (e.g. delete/inactivate).
- "Aviso Prévio" checkbox that enables/disables related date fields.
- Modern dark theme via FlatLaf with rounded components.

## Tech Stack

- **Java** (compiler source/target `25` — see `pom.xml`)
- **Java Swing** for the UI
- **[FlatLaf](https://www.formdev.com/flatlaf/) 3.5.2** for theming
- **Maven** for build and dependency management

## Project Structure

```
employee-form/
└── java dia 10 arrumado/
    └── Formulario/
        ├── pom.xml
        └── src/main/java/org/example/
            └── FuncionarioForm.java   # Main class (entry point) + all UI
```

## Getting Started

### Prerequisites

- **JDK 25** (the `pom.xml` sets `maven.compiler.source`/`target` to `25`). If you use a different JDK, adjust these values in `pom.xml`.
- **Maven 3.x**

### Build & Run

From the project directory that contains `pom.xml`:

```bash
cd "java dia 10 arrumado/Formulario"

# Compile
mvn clean compile

# Run (via the exec plugin, if configured) or run the built class:
mvn exec:java -Dexec.mainClass="org.example.FuncionarioForm"
```

Alternatively, run the `main` method of `org.example.FuncionarioForm` directly from your IDE.

> Note: FlatLaf is resolved automatically by Maven as a dependency.

## Notes

- All field values shown in the form are **hard-coded sample data** — this is a UI prototype/mockup and does not persist data to a database.
- Action buttons (`Salvar`, `Concluir`, etc.) currently show confirmation dialogs and update on-screen labels rather than saving to a backend.

## License

No license file is currently included in this repository.
