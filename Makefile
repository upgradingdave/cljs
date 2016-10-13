run: FORCE
	echo "starting figwheel ... "; \
		lein figwheel

# cljs Builds
bmr:
	./scripts/compilecljs.sh bmr

exif: 
	./scripts/compilecljs.sh exif

ics: 
	./scripts/compilecljs.sh ics

lattice: 
	./scripts/compilecljs.sh lattice

pcf: 
	./scripts/compilecljs.sh pcf

pwd:
	./scripts/compilecljs.sh pwd

resize: 
	./scripts/compilecljs.sh resize
	./scripts/updateblog.sh resize

tree: tree
	./scripts/compilecljs.sh tree

todo:
	./scripts/compilecljs.sh todo

clean: 
	echo "cleaning cljs ...";\
		lein clean
FORCE:
