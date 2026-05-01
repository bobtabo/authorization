package schema

import (
	"entgo.io/ent"
	"entgo.io/ent/dialect/entsql"
	"entgo.io/ent/schema"
	"entgo.io/ent/schema/field"
	"time"
)

// AppClient holds the schema definition for the Client entity (table: clients).
type AppClient struct {
	ent.Schema
}

func (AppClient) Annotations() []schema.Annotation {
	return []schema.Annotation{
		entsql.Annotation{Table: "clients"},
	}
}

func (AppClient) Fields() []ent.Field {
	return []ent.Field{
		field.Uint64("id").Positive().Immutable(),
		field.String("name"),
		field.String("identifier"),
		field.String("post_code").Optional(),
		field.String("pref").Optional(),
		field.String("city").Optional(),
		field.String("address").Optional(),
		field.String("building").Optional(),
		field.String("tel").Optional(),
		field.String("email").Optional(),
		field.String("access_token"),
		field.String("private_key"),
		field.String("public_key"),
		field.String("fingerprint"),
		field.Int("status").Default(1),
		field.Time("start_at").Optional().Nillable(),
		field.Time("stop_at").Optional().Nillable(),
		field.Time("created_at").Default(time.Now),
		field.Uint("created_by").Optional().Nillable(),
		field.Time("updated_at").Default(time.Now),
		field.Uint("updated_by").Optional().Nillable(),
		field.Time("deleted_at").Optional().Nillable(),
		field.Uint("deleted_by").Optional().Nillable(),
		field.Int("version").Default(0),
	}
}

func (AppClient) Edges() []ent.Edge {
	return nil
}
