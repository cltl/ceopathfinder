#How to run the CEO Pathfinder evaluation
#We first run the Pathfinder using the MentionReader class.
#This class reads the gold file, derives all mention pairs and tries to detect relations between them using the following methods:
# 0 = baseline based on the mention sequence
# 1 = using the CEO and CEO lexicon
# 2 = narrative chains
# 3 = clinks newsreader output FBK PRO module
# 4 = framenet causal relations
# using --expand results are projected to coreferential event mentions
# change the variable EXPAND to remove or add the --expand flag

# extensions
#'.eval.bl1S', '.eval.bl3S', '.eval.bl5S', '.eval.blANY', .eval.ceo1S','.eval.ceo3S', '.eval.ceo5S', '.eval.ceoANY'
# methods
# 0 = baseline, 1 = ceo, 2 = narrative chains, 3 = fbk pro clinks, 4 = fn cause

#!/usr/bin/env bash
BASE="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

BIN=$BASE/bin/ceopathfinder-v0.1-jar-with-dependencies.jar
IN=$BASE/data/gold
CEO=$BASE/resources/CEO_version_1.owl
CEOLEX=$BASE/resources/ceo-lexicon-ecb-v1.txt
CHAINS=$BASE/resources/EventChains_JurChamb_CEO.rtf
CLINKS=$BASE/data/nwr-clinks

# You need to obtain your own copy of FrameNet
FN-REL=/Resources/FrameNet/fndata-1.7/frRelation.xml
FN-LEX=/Resources/FrameNet/fndata-1.7/luIndex.xml

# comment out EXPAND to remove coreferential matches
EXPAND="expand"
#EXPAND="noexpand"

# BASELINE
OUT=$BASE/data/out-baseline
java -Xmx812m -cp $BIN MentionReader --method 0 --input $IN --output $OUT --$EXPAND

python eval_script-20180323.py $IN $OUT .eval.bl1S > out.eval.bl1S
python eval_script-20180323.py $IN $OUT .eval.bl3S > out.eval.bl3S
python eval_script-20180323.py $IN $OUT .eval.bl5S > out.eval.bl5S
python eval_script-20180323.py $IN $OUT .eval.blANY > out.eval.blANY

#CEO
#direct
OUT=$BASE/data/out-ceo-direct

java -Xmx812m -cp $BIN MentionReader --method 1 --property-threshold 1 --ontology-depth 1 --ceo-lexicon $CEOLEX --ceo-ontology $CEO --debug --input $IN --output $OUT --$EXPAND

python eval_script-20180323.py $IN $OUT .eval.ceo1S > out.eval.ceo1S
python eval_script-20180323.py $IN $OUT .eval.ceo3S > out.eval.ceo3S
python eval_script-20180323.py $IN $OUT .eval.ceo5S > out.eval.ceo5S
python eval_script-20180323.py $IN $OUT .eval.ceoANY > out.eval.ceoANY

#intermediate 1
OUT=$BASE/data/out-ceo-intermediate1

java -Xmx812m -cp $BIN MentionReader --method 1 --property-threshold 1 --ontology-depth 1 --ceo-lexicon $CEOLEX --ceo-ontology $CEO --debug --input $IN --output $OUT --intermediate 1 --$EXPAND

python eval_script-20180323.py $IN $OUT .eval.ceo1S > out.eval.ceo1Si1
python eval_script-20180323.py $IN $OUT .eval.ceo3S > out.eval.ceo3Si1
python eval_script-20180323.py $IN $OUT .eval.ceo5S > out.eval.ceo5Si1
python eval_script-20180323.py $IN $OUT .eval.ceoANY > out.eval.ceoANYi1

#intermediate 2
OUT=$BASE/data/out-ceo-intermediate2

java -Xmx812m -cp $BIN MentionReader --method 1 --property-threshold 1 --ontology-depth 1 --ceo-lexicon $CEOLEX --ceo-ontology $CEO --debug --input $IN --output $OUT --intermediate 2 --$EXPAND

python eval_script-20180323.py $IN $OUT .eval.ceo1S > out.eval.ceo1Si2
python eval_script-20180323.py $IN $OUT .eval.ceo3S > out.eval.ceo3Si2
python eval_script-20180323.py $IN $OUT .eval.ceo5S > out.eval.ceo5Si2
python eval_script-20180323.py $IN $OUT .eval.ceoANY > out.eval.ceoANYi2

# Narrative chains
OUT=$BASE/data/out-narrativechains

java -Xmx812m -cp $BIN MentionReader --method 2 --chains $CHAINS --input $IN --output $OUT --$EXPAND

python eval_script-20180323.py $IN $OUT .eval.nc1S > out.eval.nc1S
python eval_script-20180323.py $IN $OUT .eval.nc3S > out.eval.nc3S
python eval_script-20180323.py $IN $OUT .eval.nc5S > out.eval.nc5S
python eval_script-20180323.py $IN $OUT .eval.ncANY > out.eval.ncANY

# NAF clinks
OUT=$BASE/data/out-naf-clinks

java -Xmx812m -cp $BIN MentionReader --method 3 --nwr-clinks $CLINKS --input $IN --output $OUT --$EXPAND

python eval_script-20180323.py $IN $OUT .eval.clink1S > out.eval.clink1S
python eval_script-20180323.py $IN $OUT .eval.clink3S > out.eval.clink3S
python eval_script-20180323.py $IN $OUT .eval.clink5S > out.eval.clink5S
python eval_script-20180323.py $IN $OUT .eval.clinkANY > out.eval.clinkANY

# NAF clinks
OUT=$BASE/data/out-framenet

java -Xmx812m -cp $BIN MentionReader --method 4 --fn-relations $CLINKS $FN-REL --fn-lexicon $FN-LEX --input $IN --output $OUT --$EXPAND

python eval_script-20180323.py $IN $OUT .eval.fn1S > out.eval.fn1S
python eval_script-20180323.py $IN $OUT .eval.fn3S > out.eval.fn3S
python eval_script-20180323.py $IN $OUT .eval.fn5S > out.eval.fn5S
python eval_script-20180323.py $IN $OUT .eval.fnANY > out.eval.fnANY

# the next function generates a csv file aggregating the results
java -Xmx812m -cp $BIN ResultTable --input $BASE --label $EXPAND

