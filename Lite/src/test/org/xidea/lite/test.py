from Expression import evaluate
from Template import renderList
import StringIO


class Test:
    v=3
    def test(self,x):
        return x*self.v;
    def __getitem3_(self,x):
        return lambda x:x/2;
        
#el = "object.test(123)"
#el = [[VALUE_VAR,"object"],[VALUE_CONSTANTS,"test"],[OP_GET_PROP],[VALUE_NEW_LIST],[VALUE_CONSTANTS,123],[OP_PARAM_JOIN],[OP_INVOKE_METHOD]];
el = [[-1,"object"],[48,"test"],[-3],[0,123],[1,None],[81]];
print(evaluate(el,{"object":Test()}))



def test():
	output = StringIO.StringIO();
	Template(["12345",[0,[[0,1],[0,2],[11]]]]).render({},output)
	print output.getvalue()
test();
