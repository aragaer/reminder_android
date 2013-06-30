JAVA_FILES=$(shell find src -name *.java)
include config.mk

all: debug

run: deploy
	adb -e shell am start $(MAIN_ACTIVITY)

deploy: debug
#	-adb -e shell pm uninstall -k $(APP_NAME)
	adb -e install -r bin/$(APP_NAME)-debug.apk
	touch $@

debug: bin/$(APP_NAME)-debug.apk

bin/$(APP_NAME)-debug.apk: $(JAVA_FILES) build.xml
	ant debug

bin/$(APP_NAME)-release-unsigned.apk: $(JAVA_FILES) build.xml
	ant release

%-unaligned.apk: %-unsigned.apk
	jarsigner -signedjar $@ -keystore $(KEYSTORE) -sigalg SHA1withRSA -digestalg SHA1 $< jtt

%-release.apk: %-release-unaligned.apk
	$(TOOLS)/zipalign -f -v 4 $< $@

release: bin/$(APP_NAME)-release.apk

build.xml:
	$(TOOLS)/android update project -p . -n $(APP_NAME)

clean:
	ant clean

.PHONY: clean run release debug
