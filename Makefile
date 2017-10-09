SOURCES = src/main.d src/command.d src/help/help.d src/init/init.d

all:
	dmd -I./src $(SOURCES) -ofscs

clean:
	rm -rf ./scs.*

debug:
	dmd -I./src $(SOURCES) -ofscs -gc