Dot notation map/struct access. Mainly just a wrapper for github.com/oleiade/reflections, so that should get most of the credit.

Setting is in development. Getting works 100%.

Since this is intended to work on public properties, each element in the dot notation string is converted to title case if the object is a struct. They remain as-is if it's a map.

# Get

## Struct
```go
type ChildStruct struct {
	Prop string
}
type MyStruct struct {
	Nested *ChildStruct
}

myStruct := &MyStruct{
	Nested:&ChildStruct{"foo"},
}

// This will get myStruct.Nested.Prop
val, err := dotaccess.Get(myStruct, "nested.prop")
// returns "foo", nil

val, err = dotaccess.Get(myStruct, "foo.bar")
//returns nil, error
```

## Map

Maps will not return errors if the key does not exist, only nil.

```go

myMap := map[string]interface{}{
	"nested":map[string]string{
		"prop":"foo"
	}
}
val, err := dotaccess.Get(myMap, "nested.prop")
// returns "foo", nil

val, err = dotaccess.Get(myStruct, "foo.bar")
//returns nil, nil
```
