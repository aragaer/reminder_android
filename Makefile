JAVA_FILES=$(shell find src -name *.java)
include config.mk

ADB_FLAGS := -e

all: debug

run: deploy
	adb $(ADB_FLAGS) shell am start $(MAIN_ACTIVITY)

deploy: debug
	adb $(ADB_FLAGS) install -r bin/$(APP_NAME)-debug.apk
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

configure:
	-rm build.xml
	$(MAKE) build.xml

build.xml:
	$(TOOLS)/android update project -p . -n $(APP_NAME)

clean:
	ant clean

.PHONY: all configure clean run release debug
