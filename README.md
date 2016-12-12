# witan.gwyn

## Description

`witan.gwyn` is a Clojure library for calculating fire risk assessment scores based on historical fire incidents data from the London Fire Brigade (LFB).

This library makes extensive use of the tools in [witan.workspace-api](https://github.com/MastodonC/witan.workspace-api) and aims to be run on the [workspace-executor](https://github.com/MastodonC/witan.workspace-executor) on from the repl or the command line.

For more information see the [doc](doc/intro.md).

### Current status:
**Minimal version to be released**

We use Google Places API for non-residential properties information and associate a risk score based on the type of property. Risk scores for each property type is calculated using LFB historical incidents data.

## Use

### Note
This code requires a Google Places API key to be saved as an environment variable called 'PLACES_API_KEY'

You can get an API key here: https://developers.google.com/places/web-service/get-api-key

### Run the model

* **From the command-line**

The easiest way to run the model at the moment is by running the acceptance test:
```Bash
> lein test witan.gwyn.acceptance.workspace-test
```
You will see the result printed on your terminal. Note that in the test the model runs for properties around "Twickenham" fire station.

* **From the repl**

You can also run the model from your repl:
```Clojure
;; Go to the test namespace `witan.gwyn.gwyn-test`:
> (in-ns 'witan.gwyn.gwyn-test)

;; Run all the steps in the model, you can even edit the fire station name:
> (let [input-map (reduce (fn [acc func] (merge acc (func acc)))
                            test-data
                            [group-commercial-properties-type-1-0-0
                             generic-commercial-properties-fire-risk-1-0-0
                             #(extract-fire-station-geo-data-1-0-0
                               % {:fire-station "Twickenham"})
                             list-commercial-properties-1-0-0])
          result (associate-risk-score-to-commercial-properties-1-0-0 input-map)]
          (:commercial-properties-with-scores result))
```

*Note*: for more info on the model, data inputs and parameter (fire station) see the [doc](doc/into.md)

## License

Copyright © 2016 MastodonC Ltd
