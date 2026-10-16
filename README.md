# Turnos Forastero

App Android con los turnos del personal del Hotel El Forastero, por habitación:
quién entró cada viernes, quién salió cada jueves, descansos y regresos.

## Cómo se actualiza

- **Los turnos**: viven en `datos/meses.json`. Cada mes se cruzan las fotos de las
  listas semanales y se actualiza ese archivo. La app lo baja sola al abrir y cada
  6 horas (con notificación). Cambiar solo `datos/` no recompila el APK.
- **La app**: cada push a `main` compila el APK en GitHub Actions y lo publica en
  Releases (`latest`). La app detecta la versión nueva, la baja y ofrece instalarla.

## Formato de `datos/meses.json`

```json
{
  "actualizado": "2026-10-15",
  "meses": {
    "2026-09": {
      "listas": [
        { "fecha": "2026-09-04", "filas": [
          { "hab": "5", "nombre": "KEVIN FELIPE ROMERO GARCIA", "turno": "MITAD" }
        ] }
      ]
    }
  }
}
```

El repositorio es público: no se guardan cédulas ni cargos, solo habitación, nombre y turno.

Reglas del cruce: INGRESA = entra ese viernes; MITAD = segunda semana. Si alguien
ingresa y no aparece el viernes siguiente, hizo 7 días. Si aparece en MITAD sin haber
ingresado, llegó de otro campo o a cubrir y hace 7 días. Cada persona puede volver
después de su descanso en el mismo mes.

Descargar: https://github.com/franyer98/turnos-forastero/releases/download/latest/TurnosForastero.apk
