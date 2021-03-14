# Note gitlab < v9 is limited in that it only supports one image name per repo.
# This means that multiple assemblies are not yet supported without trickery.

build:
	docker build --no-cache --tag URL:VERSION .

push:
	docker push URL:VERSION