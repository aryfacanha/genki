---
name: testar-emulador
description: Compila, roda os testes unitários, instala o Genki no emulador ou celular conectado, abre o app, captura a tela e lê o logcat. Use só quando o autor pedir /testar-emulador.
disable-model-invocation: true
---

# Testar no emulador

Objetivo: checagem rápida automatizada. NÃO substitui a verificação manual do autor (ESPEC S5).
Foco opcional informado pelo autor: $ARGUMENTS

1. `adb devices` — se nenhum aparelho aparecer como "device", pare e peça ao autor para abrir o emulador
   no Device Manager ou conectar o celular.
2. `.\gradlew.bat assembleDebug test` — se falhar, mostre o erro e pare.
3. `.\gradlew.bat installDebug`
4. `adb logcat -c` e depois `adb shell am start -n br.edu.ifrs.genki/.MainActivity`; aguarde ~3 s.
5. Captura de tela (não use redirecionamento `>` no PowerShell, corrompe a imagem):
   `adb shell screencap -p /sdcard/genki_tela.png` e `adb pull /sdcard/genki_tela.png build/genki_tela.png`.
   Leia a imagem e compare com o esperado para a tarefa atual (e com o protótipo, se ele estiver em /add-dir).
6. Se o foco pedir navegação, use `adb shell input tap X Y` / `input text` e capture de novo.
7. `adb logcat -d *:E` (erros e crashes) e `adb logcat -d -s GenkiEval`.

Relatório final, sempre neste formato:
- Build e testes: passou/falhou
- Instalado em: <modelo do aparelho/emulador e API>
- O que a captura mostra x o esperado
- Erros no logcat (ou "nenhum")
- NÃO verificado: voz, câmera, alarmes reais, leitor de tela, texto ampliado — verificação manual do autor
Não altere código nesta skill. Se achar problema, descreva e pergunte se deve corrigir.
